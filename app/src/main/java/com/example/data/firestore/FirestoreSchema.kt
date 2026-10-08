package com.example.data.firestore

import com.example.model.AlertType
import com.example.model.EmergencyAlert
import com.example.model.EmergencyContact
import com.example.model.EmergencyStatus
import com.example.model.LocationData
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.UUID

/**
 * Basic Data Schema for Storing User Emergency Contact Information in Firestore.
 * Matches collection path: /users/{userId}/emergency_contacts/{contactId}
 */
data class FirestoreEmergencyContact(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val relation: String = "",
    val phone: String = "",
    val email: String? = null,
    @get:PropertyName("isPrimary")
    val isPrimary: Boolean = false,
    val priority: Int = 1,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun toDomain(): EmergencyContact {
        return EmergencyContact(
            name = name,
            relation = relation,
            phone = phone
        )
    }

    companion object {
        fun fromDomain(
            domain: EmergencyContact,
            userId: String,
            id: String = UUID.randomUUID().toString(),
            isPrimary: Boolean = false,
            priority: Int = 1
        ): FirestoreEmergencyContact {
            return FirestoreEmergencyContact(
                id = id,
                userId = userId,
                name = domain.name,
                relation = domain.relation,
                phone = domain.phone,
                isPrimary = isPrimary,
                priority = priority
            )
        }
    }
}

/**
 * Basic Data Schema for Storing SOS Alert History in Firestore.
 * Matches collection paths:
 *  - /users/{userId}/sos_history/{alertId} (per-user historical audit)
 *  - /sos_alerts/{alertId} (real-time dispatch feed for campus security)
 */
data class FirestoreSosAlert(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val studentRollNo: String = "",
    val studentClass: String = "",
    val bloodGroup: String = "",
    val triggerType: String = "SOS_CRITICAL", // SOS_CRITICAL, MEDICAL, NEED_HELP, GEOFENCE_BREACH, HIGH_RISK_CHECKIN
    val status: String = "ACTIVE", // ACTIVE, RESOLVED, CANCELLED
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val googleMapsUrl: String = "",
    val contactsNotifiedCount: Int = 0,
    val triggeredAtMillis: Long = System.currentTimeMillis(),
    val resolvedAtMillis: Long? = null,
    val resolutionNotes: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun toDomain(): EmergencyAlert {
        val alertTypeEnum = try {
            AlertType.valueOf(triggerType)
        } catch (_: Exception) {
            AlertType.SOS_CRITICAL
        }

        val statusEnum = try {
            EmergencyStatus.valueOf(status)
        } catch (_: Exception) {
            EmergencyStatus.ACTIVE
        }

        return EmergencyAlert(
            id = id,
            studentId = userId,
            studentName = studentName,
            studentClass = studentClass,
            studentPhone = "",
            bloodGroup = bloodGroup,
            isHosteller = false,
            alertType = alertTypeEnum,
            status = statusEnum,
            location = LocationData(
                latitude = latitude,
                longitude = longitude,
                address = address
            ),
            timestamp = triggeredAtMillis,
            resolutionNotes = resolutionNotes
        )
    }

    companion object {
        fun fromDomain(
            alert: EmergencyAlert,
            contactsCount: Int = 2
        ): FirestoreSosAlert {
            return FirestoreSosAlert(
                id = alert.id,
                userId = alert.studentId,
                studentName = alert.studentName,
                studentRollNo = "",
                studentClass = alert.studentClass,
                bloodGroup = alert.bloodGroup,
                triggerType = alert.alertType.name,
                status = alert.status.name,
                latitude = alert.location.latitude,
                longitude = alert.location.longitude,
                address = alert.location.address,
                googleMapsUrl = alert.location.googleMapsUrl,
                contactsNotifiedCount = contactsCount,
                triggeredAtMillis = alert.timestamp,
                resolvedAtMillis = if (alert.status != EmergencyStatus.ACTIVE) System.currentTimeMillis() else null,
                resolutionNotes = alert.resolutionNotes
            )
        }
    }
}

/**
 * Data Schema for Real-Time Location Updates in Firestore.
 * Matches collection paths:
 *  - /user_locations/{userId} (global real-time responder directory)
 *  - /users/{userId}/live_location/current (per-user persistent beacon)
 */
data class FirestoreUserLiveLocation(
    @DocumentId
    val userId: String = "",
    val studentName: String = "",
    val studentRollNo: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val altitude: Double = 0.0,
    val provider: String = "gps",
    val address: String = "",
    val googleMapsUrl: String = "",
    val isEmergencyActive: Boolean = false,
    val activeEmergencyId: String? = null,
    val batteryPct: Int = 100,
    val updateSequence: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    fun toDomain(): LocationData {
        return LocationData(
            latitude = latitude,
            longitude = longitude,
            address = address,
            accuracyMeters = accuracy,
            lastUpdated = timestamp
        )
    }

    companion object {
        fun fromCoordinates(
            userId: String,
            studentName: String,
            studentRollNo: String = "",
            latitude: Double,
            longitude: Double,
            accuracy: Float = 5.0f,
            speed: Float = 0.0f,
            bearing: Float = 0.0f,
            altitude: Double = 0.0,
            provider: String = "gps",
            address: String = "",
            isEmergencyActive: Boolean = false,
            activeEmergencyId: String? = null,
            batteryPct: Int = 100,
            updateSequence: Int = 0
        ): FirestoreUserLiveLocation {
            val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
            val resolvedAddress = address.ifBlank {
                "GPS: ${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}"
            }
            return FirestoreUserLiveLocation(
                userId = userId,
                studentName = studentName,
                studentRollNo = studentRollNo,
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy,
                speed = speed,
                bearing = bearing,
                altitude = altitude,
                provider = provider,
                address = resolvedAddress,
                googleMapsUrl = mapsUrl,
                isEmergencyActive = isEmergencyActive,
                activeEmergencyId = activeEmergencyId,
                batteryPct = batteryPct,
                updateSequence = updateSequence,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}

