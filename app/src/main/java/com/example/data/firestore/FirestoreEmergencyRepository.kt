package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository implementation for managing Emergency Contacts and SOS Alert History.
 * Supports Firebase Firestore when configured, and falls back gracefully to local
 * reactive state if Firebase is unprovisioned, missing credentials, or offline.
 */
class FirestoreEmergencyRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "FirestoreEmergencyRepo"
        private const val USERS_COLLECTION = "users"
        private const val CONTACTS_SUBCOLLECTION = "emergency_contacts"
        private const val SOS_HISTORY_SUBCOLLECTION = "sos_history"
        private const val CENTRAL_SOS_COLLECTION = "sos_alerts"
        private const val USER_LOCATIONS_COLLECTION = "user_locations"
    }

    // In-memory fallback streams for robust offline/standalone operation
    private val localSosAlerts = MutableStateFlow<List<FirestoreSosAlert>>(emptyList())
    private val localContacts = MutableStateFlow<List<FirestoreEmergencyContact>>(emptyList())
    private val localLiveLocations = MutableStateFlow<Map<String, FirestoreUserLiveLocation>>(emptyMap())

    /**
     * Safely retrieves the FirebaseFirestore instance only if FirebaseApp is initialized.
     * Prevents fatal IllegalStateException crashes when google-services.json is absent.
     */
    private fun getFirestoreSafe(): FirebaseFirestore? {
        return try {
            val appContext = context?.applicationContext
            if (appContext != null && FirebaseApp.getApps(appContext).isEmpty()) {
                return null
            }
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseFirestore is not available in current environment: ${e.message}")
            null
        }
    }

    /**
     * Store or update an emergency contact for the user in Firestore:
     * Path: /users/{userId}/emergency_contacts/{contactId}
     */
    suspend fun saveEmergencyContact(contact: FirestoreEmergencyContact): Result<String> {
        val contactId = if (contact.id.isNotBlank()) contact.id else UUID.randomUUID().toString()
        val payload = contact.copy(id = contactId)

        // Always update local reactive state
        localContacts.update { current ->
            listOf(payload) + current.filter { it.id != contactId }
        }

        val db = getFirestoreSafe()
        if (db == null) {
            Log.d(TAG, "Saved emergency contact $contactId locally (Firestore unprovisioned)")
            return Result.success(contactId)
        }

        return try {
            val docRef = db.collection(USERS_COLLECTION)
                .document(contact.userId)
                .collection(CONTACTS_SUBCOLLECTION)
                .document(contactId)

            docRef.set(payload).await()
            Log.d(TAG, "Saved emergency contact $contactId to Firestore for user ${contact.userId}")
            Result.success(contactId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save emergency contact to Firestore", e)
            Result.success(contactId) // Local save succeeded
        }
    }

    /**
     * Delete an emergency contact from Firestore.
     */
    suspend fun deleteEmergencyContact(userId: String, contactId: String): Result<Unit> {
        localContacts.update { current -> current.filter { it.id != contactId } }

        val db = getFirestoreSafe()
        if (db == null) {
            return Result.success(Unit)
        }

        return try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(CONTACTS_SUBCOLLECTION)
                .document(contactId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete emergency contact $contactId", e)
            Result.success(Unit)
        }
    }

    /**
     * Real-time listener for user emergency contacts.
     */
    fun observeEmergencyContacts(userId: String): Flow<List<FirestoreEmergencyContact>> {
        val db = getFirestoreSafe()
        if (db == null || userId.isBlank()) {
            return localContacts.map { list ->
                if (userId.isBlank()) list else list.filter { it.userId == userId }
            }
        }

        return callbackFlow {
            val listenerRegistration = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(CONTACTS_SUBCOLLECTION)
                .orderBy("priority", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for emergency contacts", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val contacts = snapshot.toObjects(FirestoreEmergencyContact::class.java)
                        trySend(contacts)
                    }
                }

            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Record a newly triggered SOS alert in:
     * 1. The user's private SOS history: /users/{userId}/sos_history/{alertId}
     * 2. The central SOS feed for dispatchers: /sos_alerts/{alertId}
     */
    suspend fun recordSosAlert(alert: FirestoreSosAlert): Result<String> {
        val alertId = if (alert.id.isNotBlank()) alert.id else UUID.randomUUID().toString()
        val payload = alert.copy(id = alertId)

        // Keep in local stream
        localSosAlerts.update { list ->
            listOf(payload) + list.filter { it.id != alertId }
        }

        val db = getFirestoreSafe()
        if (db == null) {
            Log.d(TAG, "Recorded SOS Alert $alertId locally (Firestore unprovisioned)")
            return Result.success(alertId)
        }

        return try {
            val batch = db.batch()

            val userHistoryRef = db.collection(USERS_COLLECTION)
                .document(alert.userId)
                .collection(SOS_HISTORY_SUBCOLLECTION)
                .document(alertId)

            val centralRef = db.collection(CENTRAL_SOS_COLLECTION)
                .document(alertId)

            batch.set(userHistoryRef, payload)
            batch.set(centralRef, payload)
            batch.commit().await()

            Log.d(TAG, "Recorded SOS Alert $alertId to Firestore")
            Result.success(alertId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record SOS Alert to Firestore", e)
            Result.success(alertId)
        }
    }

    /**
     * Update the status of an existing SOS alert (e.g. RESOLVED or CANCELLED)
     */
    suspend fun updateSosAlertStatus(
        userId: String,
        alertId: String,
        newStatus: String,
        resolutionNotes: String? = null
    ): Result<Unit> {
        localSosAlerts.update { list ->
            list.map {
                if (it.id == alertId) {
                    it.copy(
                        status = newStatus,
                        resolvedAtMillis = System.currentTimeMillis(),
                        resolutionNotes = resolutionNotes ?: it.resolutionNotes
                    )
                } else it
            }
        }

        val db = getFirestoreSafe()
        if (db == null) {
            return Result.success(Unit)
        }

        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "resolvedAtMillis" to System.currentTimeMillis()
            )
            if (resolutionNotes != null) {
                updates["resolutionNotes"] = resolutionNotes
            }

            val batch = db.batch()
            val userHistoryRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SOS_HISTORY_SUBCOLLECTION)
                .document(alertId)

            val centralRef = db.collection(CENTRAL_SOS_COLLECTION)
                .document(alertId)

            batch.update(userHistoryRef, updates)
            batch.update(centralRef, updates)
            batch.commit().await()

            Log.d(TAG, "Updated SOS Alert $alertId to $newStatus in Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update SOS Alert $alertId in Firestore", e)
            Result.success(Unit)
        }
    }

    /**
     * Real-time listener for user's SOS alert history log.
     */
    fun observeSosAlertHistory(userId: String): Flow<List<FirestoreSosAlert>> {
        val db = getFirestoreSafe()
        if (db == null || userId.isBlank()) {
            return localSosAlerts.map { list -> list.filter { it.userId == userId } }
        }

        return callbackFlow {
            val listenerRegistration = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SOS_HISTORY_SUBCOLLECTION)
                .orderBy("triggeredAtMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for SOS alert history", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val alerts = snapshot.toObjects(FirestoreSosAlert::class.java)
                        trySend(alerts)
                    }
                }

            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Real-time listener for central active SOS alerts stream (for teachers/admins).
     */
    fun observeActiveCentralSosAlerts(): Flow<List<FirestoreSosAlert>> {
        val db = getFirestoreSafe()
        if (db == null) {
            return localSosAlerts.map { list -> list.filter { it.status == "ACTIVE" } }
        }

        return callbackFlow {
            val listenerRegistration = db.collection(CENTRAL_SOS_COLLECTION)
                .whereEqualTo("status", "ACTIVE")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for active central SOS alerts", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val alerts = snapshot.toObjects(FirestoreSosAlert::class.java)
                        trySend(alerts)
                    }
                }

            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Push periodic live location update for a user to Firestore.
     * Updates:
     *  1. /user_locations/{userId} (central tracking directory)
     *  2. /users/{userId}/live_location/current (per-user subcollection)
     *  3. If activeEmergencyId is provided, updates live coordinates in /sos_alerts/{alertId}
     */
    suspend fun updateUserLiveLocation(location: FirestoreUserLiveLocation): Result<Unit> {
        val userId = location.userId
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID must not be blank for location tracking"))
        }

        // Always update local memory fallback stream
        localLiveLocations.update { current ->
            current + (userId to location)
        }

        val db = getFirestoreSafe()
        if (db == null) {
            Log.d(TAG, "Updated live location locally for $userId: (${location.latitude}, ${location.longitude}) [Firestore unprovisioned]")
            return Result.success(Unit)
        }

        return try {
            val batch = db.batch()

            // 1. Central /user_locations/{userId}
            val locationDocRef = db.collection(USER_LOCATIONS_COLLECTION).document(userId)
            batch.set(locationDocRef, location)

            // 2. User subcollection /users/{userId}/live_location/current
            val userLiveRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection("live_location")
                .document("current")
            batch.set(userLiveRef, location)

            // 3. User summary fields on /users/{userId}
            val userSummaryRef = db.collection(USERS_COLLECTION).document(userId)
            val summaryUpdates = mapOf<String, Any>(
                "lastLatitude" to location.latitude,
                "lastLongitude" to location.longitude,
                "lastAddress" to location.address,
                "lastLocationTimestamp" to location.timestamp,
                "isEmergencyActive" to location.isEmergencyActive
            )
            batch.set(userSummaryRef, summaryUpdates, com.google.firebase.firestore.SetOptions.merge())

            // 4. If an active emergency is linked, update coordinates on active SOS alert
            val emergencyId = location.activeEmergencyId
            if (!emergencyId.isNullOrBlank()) {
                val sosAlertRef = db.collection(CENTRAL_SOS_COLLECTION).document(emergencyId)
                val sosUpdates = mapOf<String, Any>(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "address" to location.address,
                    "googleMapsUrl" to location.googleMapsUrl,
                    "lastTrackingUpdate" to location.timestamp
                )
                batch.update(sosAlertRef, sosUpdates)
            }

            batch.commit().await()
            Log.d(TAG, "Pushed live location to Firestore for $userId: (${location.latitude}, ${location.longitude})")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update live location in Firestore for $userId", e)
            Result.success(Unit) // Handled gracefully via local stream
        }
    }

    /**
     * Real-time listener for a specific user's live location stream.
     */
    fun observeUserLiveLocation(userId: String): Flow<FirestoreUserLiveLocation?> {
        val db = getFirestoreSafe()
        if (db == null || userId.isBlank()) {
            return localLiveLocations.map { map -> map[userId] }
        }

        return callbackFlow {
            val listenerRegistration = db.collection(USER_LOCATIONS_COLLECTION)
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for user live location: $userId", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val loc = snapshot.toObject(FirestoreUserLiveLocation::class.java)
                        trySend(loc)
                    } else {
                        trySend(localLiveLocations.value[userId])
                    }
                }

            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Real-time listener for all active user locations (for teachers/admins live monitoring map).
     */
    fun observeAllUserLocations(): Flow<List<FirestoreUserLiveLocation>> {
        val db = getFirestoreSafe()
        if (db == null) {
            return localLiveLocations.map { map -> map.values.toList() }
        }

        return callbackFlow {
            val listenerRegistration = db.collection(USER_LOCATIONS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for all user locations", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val locations = snapshot.toObjects(FirestoreUserLiveLocation::class.java)
                        trySend(locations)
                    }
                }

            awaitClose { listenerRegistration.remove() }
        }
    }
}
