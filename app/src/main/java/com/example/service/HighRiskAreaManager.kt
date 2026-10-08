package com.example.service

import android.content.Context
import com.example.data.SelrRepository
import com.example.data.local.EmergencyContactRepository
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class HighRiskAreaManager(
    private val context: Context,
    private val repository: SelrRepository,
    private val contactRepository: EmergencyContactRepository,
    private val notificationHelper: EmergencyNotificationHelper,
    private val externalScope: CoroutineScope
) {
    private val _monitoringState = MutableStateFlow(HighRiskMonitoringState())
    val monitoringState: StateFlow<HighRiskMonitoringState> = _monitoringState.asStateFlow()

    private var recurringTaskJob: Job? = null
    private var tickerJob: Job? = null

    companion object {
        const val STANDARD_INTERVAL_SECONDS = 30 * 60 // 30 minutes
        const val FAST_TEST_INTERVAL_SECONDS = 30     // 30 seconds for testing
    }

    init {
        // Observe current location to auto-detect if user enters a high-risk zone
        externalScope.launch {
            repository.currentLocation.collect { loc ->
                checkLocationRisk(loc)
            }
        }
    }

    fun checkLocationRisk(loc: LocationData) {
        val detectedZone = repository.findActiveHighRiskZone(loc)
        val currentState = _monitoringState.value

        if (detectedZone != null) {
            // Student is inside a high-risk zone
            if (!currentState.isInsideHighRiskArea || currentState.activeZone?.id != detectedZone.id) {
                onEnterHighRiskZone(detectedZone, loc)
            }
        } else if (currentState.isInsideHighRiskArea && !isSimulated) {
            // Student moved out of high-risk zone
            onExitHighRiskZone()
        }
    }

    private var isSimulated = false

    fun simulateEnterHighRiskZone(zone: HighRiskZone) {
        isSimulated = true
        val loc = repository.currentLocation.value.copy(
            latitude = zone.latitude,
            longitude = zone.longitude,
            address = "${zone.name}, Delhi NCR"
        )
        repository.updateLocation(loc)
        onEnterHighRiskZone(zone, loc)
    }

    fun simulateExitHighRiskZone() {
        isSimulated = false
        val loc = LocationData(
            latitude = 28.5450,
            longitude = 77.1926,
            address = "Gate 3, Engineering Complex, Hauz Khas, New Delhi - 110016",
            accuracyMeters = 3.8f
        )
        repository.updateLocation(loc)
        onExitHighRiskZone()
    }

    fun toggleTestInterval() {
        val newTestMode = !_monitoringState.value.isFastTestInterval
        _monitoringState.value = _monitoringState.value.copy(
            isFastTestInterval = newTestMode,
            nextUpdateSecondsRemaining = if (newTestMode) FAST_TEST_INTERVAL_SECONDS else STANDARD_INTERVAL_SECONDS
        )
    }

    private fun onEnterHighRiskZone(zone: HighRiskZone, location: LocationData) {
        val intervalSeconds = if (_monitoringState.value.isFastTestInterval) FAST_TEST_INTERVAL_SECONDS else STANDARD_INTERVAL_SECONDS

        _monitoringState.value = _monitoringState.value.copy(
            isInsideHighRiskArea = true,
            activeZone = zone,
            nextUpdateSecondsRemaining = intervalSeconds
        )

        // Dispatch initial 'Safe' status update upon entering high-risk area
        dispatchSafeStatusUpdate(zone, location, isInitialEntry = true)

        // Start 30-minute recurring background monitoring
        startRecurringMonitoringTask()
    }

    private fun onExitHighRiskZone() {
        recurringTaskJob?.cancel()
        tickerJob?.cancel()

        _monitoringState.value = _monitoringState.value.copy(
            isInsideHighRiskArea = false,
            activeZone = null,
            nextUpdateSecondsRemaining = STANDARD_INTERVAL_SECONDS
        )
    }

    private fun startRecurringMonitoringTask() {
        recurringTaskJob?.cancel()
        tickerJob?.cancel()

        val getInterval = {
            if (_monitoringState.value.isFastTestInterval) FAST_TEST_INTERVAL_SECONDS else STANDARD_INTERVAL_SECONDS
        }

        // Background countdown ticker
        tickerJob = externalScope.launch {
            while (isActive && _monitoringState.value.isInsideHighRiskArea) {
                delay(1000)
                val currentRemaining = _monitoringState.value.nextUpdateSecondsRemaining
                if (currentRemaining > 1) {
                    _monitoringState.value = _monitoringState.value.copy(
                        nextUpdateSecondsRemaining = currentRemaining - 1
                    )
                } else {
                    // 30 minutes expired! Send recurring 'Safe' update
                    val activeZone = _monitoringState.value.activeZone
                    if (activeZone != null) {
                        dispatchSafeStatusUpdate(
                            activeZone,
                            repository.currentLocation.value,
                            isInitialEntry = false
                        )
                    }
                    _monitoringState.value = _monitoringState.value.copy(
                        nextUpdateSecondsRemaining = getInterval()
                    )
                }
            }
        }
    }

    fun sendManualSafeStatusUpdate() {
        val activeZone = _monitoringState.value.activeZone ?: repository.highRiskZones.value.first()
        val loc = repository.currentLocation.value
        dispatchSafeStatusUpdate(activeZone, loc, isInitialEntry = false)
        val interval = if (_monitoringState.value.isFastTestInterval) FAST_TEST_INTERVAL_SECONDS else STANDARD_INTERVAL_SECONDS
        _monitoringState.value = _monitoringState.value.copy(
            nextUpdateSecondsRemaining = interval
        )
    }

    private fun dispatchSafeStatusUpdate(
        zone: HighRiskZone,
        location: LocationData,
        isInitialEntry: Boolean
    ) {
        externalScope.launch {
            val student = repository.studentProfile.value
            // Fetch registered emergency contacts from Room database
            val roomContacts = contactRepository.allContacts.first()
            val phoneList = if (roomContacts.isNotEmpty()) {
                roomContacts.map { it.phone }
            } else {
                listOf(student.emergencyContact1.phone, student.emergencyContact2.phone)
            }

            val intervalMinutes = if (_monitoringState.value.isFastTestInterval) 1 else 30
            val smsMessage = notificationHelper.formatSafeStatusSms(
                studentName = student.fullName,
                studentClass = student.classRollNo,
                zoneName = zone.name,
                location = location,
                nextIntervalMinutes = intervalMinutes
            )

            // Auto-send SMS to registered contacts
            notificationHelper.sendEmergencySmsIntent(phoneList, smsMessage)

            // Post system notification to device
            notificationHelper.showSafeStatusNotification(
                studentName = student.fullName,
                zoneName = zone.name,
                address = location.address,
                contactsCount = phoneList.size,
                nextIntervalMinutes = intervalMinutes
            )

            // Log update in repository history
            val updateRecord = SafeStatusUpdate(
                id = "safe_upd_${System.currentTimeMillis()}",
                studentName = student.fullName,
                timestamp = System.currentTimeMillis(),
                zoneName = zone.name,
                location = location,
                recipientsCount = phoneList.size,
                recipientPhoneList = phoneList,
                messageText = smsMessage,
                deliveryStatus = if (isInitialEntry) "HIGH-RISK ENTRY UPDATE SENT" else "30-MIN RECURRING UPDATE SENT"
            )
            repository.addSafeStatusUpdate(updateRecord)

            _monitoringState.value = _monitoringState.value.copy(
                totalUpdatesSent = _monitoringState.value.totalUpdatesSent + 1,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        }
    }
}
