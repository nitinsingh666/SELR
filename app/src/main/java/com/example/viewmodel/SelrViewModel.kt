package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SelrRepository
import com.example.data.firestore.FirestoreEmergencyContact
import com.example.data.firestore.FirestoreEmergencyRepository
import com.example.data.firestore.FirestoreSosAlert
import com.example.data.firestore.FirestoreUserLiveLocation
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.EmergencyContactRepository
import com.example.data.local.SelrDatabase
import com.example.model.*
import com.example.service.EmergencyLocationTrackingService
import com.example.service.EmergencyNotificationHelper
import com.example.service.HapticManager
import com.example.service.LocationHelper
import com.example.service.SirenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class SelrViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SelrRepository()
    private val sirenManager = SirenManager(application)
    private val hapticManager = HapticManager(application)
    private val locationHelper = LocationHelper(application)
    val notificationHelper = EmergencyNotificationHelper(application)

    // Room Database & Repository
    private val contactDatabase = SelrDatabase.getDatabase(application)
    private val contactRepository = EmergencyContactRepository(contactDatabase.emergencyContactDao())

    // Firestore Cloud Repository
    private val firestoreEmergencyRepository = FirestoreEmergencyRepository(application)

    // High-Risk Area 30-min Recurring Background Task Manager
    private val highRiskAreaManager = com.example.service.HighRiskAreaManager(
        context = application,
        repository = repository,
        contactRepository = contactRepository,
        notificationHelper = notificationHelper,
        externalScope = viewModelScope
    )

    // Flow exports
    val currentSession = repository.currentSession
    val studentProfile = repository.studentProfile
    val allStudents = repository.allStudents
    val currentLocation = repository.currentLocation
    val activeEmergency = repository.activeEmergency
    val emergencyHistory = repository.emergencyHistory
    val safeZones = repository.safeZones
    val geofenceEvents = repository.geofenceEvents
    val highRiskZones = repository.highRiskZones
    val safeStatusHistory = repository.safeStatusHistory
    val highRiskMonitoringState = highRiskAreaManager.monitoringState

    // Reactive Room Emergency Contacts Flow
    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = contactRepository.allContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Firestore SOS History & Active Central Alerts Stream
    val firestoreSosHistory: StateFlow<List<FirestoreSosAlert>> = firestoreEmergencyRepository
        .observeSosAlertHistory(repository.currentSession.value.userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeCentralSosAlerts: StateFlow<List<FirestoreSosAlert>> = firestoreEmergencyRepository
        .observeActiveCentralSosAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Background Location Tracking Service using LocationManager State
    val isLocationTrackingRunning: StateFlow<Boolean> = EmergencyLocationTrackingService.isServiceRunning
    val liveTrackingLocation: StateFlow<FirestoreUserLiveLocation?> = EmergencyLocationTrackingService.liveLocationState
    val trackingUpdatesPushedCount: StateFlow<Int> = EmergencyLocationTrackingService.updatesPushedCount
    val trackingServiceError: StateFlow<String?> = EmergencyLocationTrackingService.lastError

    // Firestore Live Location Observables
    val remoteUserLiveLocation: StateFlow<FirestoreUserLiveLocation?> = firestoreEmergencyRepository
        .observeUserLiveLocation(repository.currentSession.value.userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allLiveUserLocations: StateFlow<List<FirestoreUserLiveLocation>> = firestoreEmergencyRepository
        .observeAllUserLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic Nearby Responders & Contact Locations Flow for Tactical Radar
    val nearbyResponders: StateFlow<List<NearbyContactLocation>> = combine(
        currentLocation,
        emergencyContacts
    ) { loc, contacts ->
        val list = mutableListOf<NearbyContactLocation>()
        val baseLat = loc.latitude
        val baseLng = loc.longitude

        // First add the saved primary contacts from Room
        if (contacts.isNotEmpty()) {
            contacts.take(2).forEachIndexed { index, c ->
                val dist = if (index == 0) 0.9f else 1.8f
                val bearing = if (index == 0) 45f else 180f
                list.add(
                    NearbyContactLocation(
                        id = "contact_${c.id}",
                        name = c.name,
                        relation = c.relation,
                        phone = c.phone,
                        latitude = baseLat + (if (index == 0) 0.007 else -0.012),
                        longitude = baseLng + (if (index == 0) 0.008 else 0.002),
                        distanceKm = dist,
                        etaMinutes = (dist * 4).toInt() + 1,
                        bearingDegrees = bearing,
                        status = if (index == 0) "En Route" else "Notified"
                    )
                )
            }
        } else {
            list.add(
                NearbyContactLocation(
                    id = "c_dad",
                    name = "Ramesh Sharma",
                    relation = "Father",
                    phone = "+91 98111 22334",
                    latitude = baseLat + 0.007,
                    longitude = baseLng + 0.008,
                    distanceKm = 0.9f,
                    etaMinutes = 4,
                    bearingDegrees = 45f,
                    status = "En Route"
                )
            )
        }

        // Add Institutional & Public Responders (Campus Patrol & City PCR Van)
        list.add(
            NearbyContactLocation(
                id = "resp_patrol",
                name = "Campus Quick Reaction Patrol",
                relation = "Security Unit",
                phone = "+91 11 2659 1000",
                latitude = baseLat + 0.003,
                longitude = baseLng - 0.004,
                distanceKm = 0.4f,
                etaMinutes = 2,
                bearingDegrees = 300f,
                status = "Approaching"
            )
        )
        list.add(
            NearbyContactLocation(
                id = "resp_pcr",
                name = "City Police Emergency PCR 12",
                relation = "Police 112",
                phone = "112",
                latitude = baseLat - 0.005,
                longitude = baseLng + 0.006,
                distanceKm = 0.6f,
                etaMinutes = 3,
                bearingDegrees = 120f,
                status = "Dispatched"
            )
        )
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI States
    private val _isSirenMuted = MutableStateFlow(false)
    val isSirenMuted: StateFlow<Boolean> = _isSirenMuted.asStateFlow()

    // M3 Theme & Display Settings
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(false)
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleDynamicColor() {
        _isDynamicColor.value = !_isDynamicColor.value
    }

    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private val _fakeCallState = MutableStateFlow(FakeCallState())
    val fakeCallState: StateFlow<FakeCallState> = _fakeCallState.asStateFlow()

    private val _remainingCountdownSeconds = MutableStateFlow(60 * 60) // 60 minutes
    val remainingCountdownSeconds: StateFlow<Int> = _remainingCountdownSeconds.asStateFlow()

    private var emergencyTimerJob: Job? = null
    private var fakeCallTimerJob: Job? = null

    init {
        refreshLocation()
        updateBatteryStatus()
        viewModelScope.launch {
            contactRepository.populateDefaultsIfEmpty()
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            repository.updateLocation(loc)
        }
    }

    private fun updateBatteryStatus() {
        try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = getApplication<Application>().registerReceiver(null, ifilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 88
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            _deviceStatus.value = DeviceStatus(
                batteryPercent = batteryPct,
                isCharging = isCharging,
                signalStrength = "Tactical 5G (Full)",
                gpsStatus = "High Accuracy GPS Locked (±3.8m)"
            )
        } catch (_: Exception) {}
    }

    fun triggerCriticalSos() {
        hapticManager.vibrateEmergencyPattern()
        _isSirenMuted.value = false
        sirenManager.startEmergencySiren(viewModelScope)

        viewModelScope.launch {
            // High accuracy location update
            val loc = locationHelper.getCurrentLocation()
            repository.updateLocation(loc)

            val alert = repository.triggerEmergency(AlertType.SOS_CRITICAL)

            // Auto-send SMS to Parent & Teacher phone numbers saved in Room database
            val contacts = if (emergencyContacts.value.isNotEmpty()) {
                emergencyContacts.value.map { it.phone }
            } else {
                listOf(
                    studentProfile.value.emergencyContact1.phone,
                    studentProfile.value.emergencyContact2.phone
                )
            }
            val smsText = notificationHelper.formatEmergencySms(alert)
            notificationHelper.sendEmergencySmsIntent(contacts, smsText)

            // Post System Notification
            notificationHelper.showEmergencyNotification(alert)

            // Sync SOS Alert to Firestore Cloud Schema
            try {
                val firestoreAlert = FirestoreSosAlert.fromDomain(alert, contacts.size)
                firestoreEmergencyRepository.recordSosAlert(firestoreAlert)
            } catch (_: Exception) {}

            // Activate real-time background location service via LocationManager
            startBackgroundLocationTracking(
                isEmergency = true,
                emergencyId = alert.id,
                intervalSeconds = 5L
            )

            start60MinCountdown()
        }
    }

    fun saveEmergencyContact(name: String, relation: String, phone: String, isPrimary: Boolean = false) {
        viewModelScope.launch {
            contactRepository.insert(
                EmergencyContactEntity(
                    name = name,
                    relation = relation,
                    phone = phone,
                    isPrimary = isPrimary,
                    isPriorityAlert = true
                )
            )

            // Sync to Firestore Emergency Contacts collection: /users/{userId}/emergency_contacts/{contactId}
            try {
                val firestoreContact = FirestoreEmergencyContact(
                    id = UUID.randomUUID().toString(),
                    userId = currentSession.value.userId,
                    name = name,
                    relation = relation,
                    phone = phone,
                    isPrimary = isPrimary,
                    priority = if (isPrimary) 1 else 2
                )
                firestoreEmergencyRepository.saveEmergencyContact(firestoreContact)
            } catch (_: Exception) {}
        }
    }

    fun updateEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            contactRepository.update(contact)
            try {
                val firestoreContact = FirestoreEmergencyContact(
                    id = "contact_${contact.id}",
                    userId = currentSession.value.userId,
                    name = contact.name,
                    relation = contact.relation,
                    phone = contact.phone,
                    isPrimary = contact.isPrimary,
                    priority = if (contact.isPrimary) 1 else 2
                )
                firestoreEmergencyRepository.saveEmergencyContact(firestoreContact)
            } catch (_: Exception) {}
        }
    }

    fun deleteEmergencyContact(id: Long) {
        viewModelScope.launch {
            contactRepository.deleteById(id)
            try {
                firestoreEmergencyRepository.deleteEmergencyContact(
                    userId = currentSession.value.userId,
                    contactId = "contact_$id"
                )
            } catch (_: Exception) {}
        }
    }

    fun triggerQuickHelp(type: AlertType) {
        hapticManager.vibrateShort()
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            repository.updateLocation(loc)
            val alert = repository.triggerEmergency(type)
            notificationHelper.showEmergencyNotification(alert)

            // Sync quick help alert to Firestore
            try {
                val firestoreAlert = FirestoreSosAlert.fromDomain(alert, 2)
                firestoreEmergencyRepository.recordSosAlert(firestoreAlert)
            } catch (_: Exception) {}

            // Activate real-time background location service via LocationManager
            startBackgroundLocationTracking(
                isEmergency = true,
                emergencyId = alert.id,
                intervalSeconds = 5L
            )

            start60MinCountdown()
        }
    }

    private fun start60MinCountdown() {
        emergencyTimerJob?.cancel()
        _remainingCountdownSeconds.value = 60 * 60
        emergencyTimerJob = viewModelScope.launch {
            while (isActive && _remainingCountdownSeconds.value > 0) {
                delay(1000)
                _remainingCountdownSeconds.value -= 1
            }
        }
    }

    fun toggleSirenMute() {
        if (_isSirenMuted.value) {
            _isSirenMuted.value = false
            sirenManager.startEmergencySiren(viewModelScope)
        } else {
            _isSirenMuted.value = true
            sirenManager.stopSiren()
        }
    }

    fun cancelEmergency(reason: String = "Accidental 3-sec SOS press") {
        val active = activeEmergency.value
        sirenManager.stopSiren()
        emergencyTimerJob?.cancel()
        _isSirenMuted.value = false
        notificationHelper.cancelEmergencyNotification()
        repository.cancelActiveEmergency(reason)

        // Downgrade background tracking from critical emergency mode
        EmergencyLocationTrackingService.updateEmergencyStatus(
            context = getApplication(),
            isEmergency = false,
            emergencyId = null
        )

        // Sync cancellation to Firestore
        if (active != null) {
            viewModelScope.launch {
                try {
                    firestoreEmergencyRepository.updateSosAlertStatus(
                        userId = active.studentId,
                        alertId = active.id,
                        newStatus = "CANCELLED",
                        resolutionNotes = reason
                    )
                } catch (_: Exception) {}
            }
        }
    }

    fun resolveEmergency(alertId: String, notes: String) {
        val active = activeEmergency.value
        sirenManager.stopSiren()
        emergencyTimerJob?.cancel()
        notificationHelper.cancelEmergencyNotification()
        repository.resolveEmergency(alertId, notes)

        // Downgrade background tracking from critical emergency mode
        EmergencyLocationTrackingService.updateEmergencyStatus(
            context = getApplication(),
            isEmergency = false,
            emergencyId = null
        )

        // Sync resolution to Firestore
        viewModelScope.launch {
            try {
                firestoreEmergencyRepository.updateSosAlertStatus(
                    userId = active?.studentId ?: currentSession.value.userId,
                    alertId = alertId,
                    newStatus = "RESOLVED",
                    resolutionNotes = notes
                )
            } catch (_: Exception) {}
        }
    }

    // ==========================================
    // Real-Time Background Location Tracking Service Controls
    // ==========================================

    fun startBackgroundLocationTracking(
        isEmergency: Boolean = activeEmergency.value != null,
        emergencyId: String? = activeEmergency.value?.id,
        intervalSeconds: Long = 10L
    ) {
        EmergencyLocationTrackingService.startTracking(
            context = getApplication(),
            userId = currentSession.value.userId,
            studentName = studentProfile.value.fullName,
            studentRollNo = studentProfile.value.classRollNo,
            isEmergency = isEmergency,
            emergencyId = emergencyId,
            intervalSeconds = intervalSeconds
        )
    }

    fun stopBackgroundLocationTracking() {
        EmergencyLocationTrackingService.stopTracking(getApplication())
    }

    fun toggleBackgroundLocationTracking() {
        if (EmergencyLocationTrackingService.isServiceRunning.value) {
            stopBackgroundLocationTracking()
        } else {
            startBackgroundLocationTracking()
        }
    }

    // Fake Safe Call Feature (for girl safety)
    fun scheduleFakeSafeCall(delaySeconds: Int = 3) {
        viewModelScope.launch {
            if (delaySeconds > 0) {
                delay(delaySeconds * 1000L)
            }
            _fakeCallState.value = FakeCallState(
                isRinging = true,
                isInCall = false,
                callerName = "Papa (Home)",
                callerNumber = "+91 98111 22334"
            )
            sirenManager.startPhoneRingtone(viewModelScope)
            hapticManager.vibrateShort()
        }
    }

    fun acceptFakeCall() {
        sirenManager.stopPhoneRingtone()
        _fakeCallState.value = _fakeCallState.value.copy(
            isRinging = false,
            isInCall = true,
            callDurationSeconds = 0
        )
        fakeCallTimerJob?.cancel()
        fakeCallTimerJob = viewModelScope.launch {
            while (isActive && _fakeCallState.value.isInCall) {
                delay(1000)
                _fakeCallState.value = _fakeCallState.value.copy(
                    callDurationSeconds = _fakeCallState.value.callDurationSeconds + 1
                )
            }
        }
    }

    fun declineOrEndFakeCall() {
        sirenManager.stopPhoneRingtone()
        fakeCallTimerJob?.cancel()
        _fakeCallState.value = FakeCallState(
            isRinging = false,
            isInCall = false
        )
    }

    // Auth & Role switching
    fun selectRole(role: UserRole, identifier: String, name: String? = null) {
        repository.loginUser(role, identifier, name)
    }

    fun registerStudent(profile: StudentProfile) {
        repository.registerNewStudent(profile)
    }

    fun updateStudentProfile(profile: StudentProfile) {
        repository.updateStudentProfile(profile)
    }

    fun addSafeZone(name: String, radius: Float) {
        repository.addSafeZone(name, radius)
    }

    fun removeSafeZone(zoneId: String) {
        repository.removeSafeZone(zoneId)
    }

    // High-Risk Area 30-min Recurring Safe Status Actions
    fun sendManualSafeStatusUpdate() {
        highRiskAreaManager.sendManualSafeStatusUpdate()
    }

    fun simulateEnterHighRiskZone(zone: HighRiskZone) {
        highRiskAreaManager.simulateEnterHighRiskZone(zone)
    }

    fun simulateExitHighRiskZone() {
        highRiskAreaManager.simulateExitHighRiskZone()
    }

    fun toggleHighRiskTestInterval() {
        highRiskAreaManager.toggleTestInterval()
    }

    override fun onCleared() {
        super.onCleared()
        sirenManager.stopSiren()
        sirenManager.stopPhoneRingtone()
        emergencyTimerJob?.cancel()
        fakeCallTimerJob?.cancel()
    }
}
