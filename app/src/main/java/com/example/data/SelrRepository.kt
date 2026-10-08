package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SelrRepository {

    // Default Demo Students
    private val defaultStudent = StudentProfile(
        id = "stu_001",
        fullName = "Aarav Sharma",
        classRollNo = "CS-3A / Roll 42",
        schoolCollegeName = "Delhi Institute of Engineering & Technology",
        bloodGroup = "O+",
        isHosteller = false, // Day-Scholar
        mobile = "+91 98765 43210",
        emergencyContact1 = EmergencyContact(
            name = "Ramesh Sharma",
            relation = "Father",
            phone = "+91 98111 22334"
        ),
        emergencyContact2 = EmergencyContact(
            name = "Sunita Sharma",
            relation = "Mother",
            phone = "+91 98222 33445"
        ),
        medicalNotes = "Mild Asthma. Carries inhaler in side pocket.",
        avatarInitial = "A"
    )

    private val sampleStudents = listOf(
        defaultStudent,
        StudentProfile(
            id = "stu_002",
            fullName = "Priya Patel",
            classRollNo = "IT-2B / Roll 18",
            schoolCollegeName = "Delhi Institute of Engineering & Technology",
            bloodGroup = "B+",
            isHosteller = true, // Hosteller
            mobile = "+91 98765 11223",
            emergencyContact1 = EmergencyContact(
                name = "Kishore Patel",
                relation = "Father",
                phone = "+91 99887 76655"
            ),
            emergencyContact2 = EmergencyContact(
                name = "Warden Mrs. Roy",
                relation = "Hostel Warden",
                phone = "+91 91234 56789"
            ),
            medicalNotes = "Penicillin Allergy",
            avatarInitial = "P"
        ),
        StudentProfile(
            id = "stu_003",
            fullName = "Rohan Verma",
            classRollNo = "ECE-4A / Roll 05",
            schoolCollegeName = "Delhi Institute of Engineering & Technology",
            bloodGroup = "A+",
            isHosteller = false,
            mobile = "+91 97654 32109",
            emergencyContact1 = EmergencyContact(
                name = "Anil Verma",
                relation = "Father",
                phone = "+91 98333 44556"
            ),
            emergencyContact2 = EmergencyContact(
                name = "Dr. S. K. Gupta",
                relation = "Family Physician",
                phone = "+91 98444 55667"
            ),
            medicalNotes = "No known medical issues",
            avatarInitial = "R"
        )
    )

    // Current Session
    private val _currentSession = MutableStateFlow(
        UserSession(
            userId = "usr_student_01",
            role = UserRole.STUDENT,
            displayName = defaultStudent.fullName,
            emailOrPhone = defaultStudent.mobile,
            linkedStudentId = defaultStudent.id
        )
    )
    val currentSession: StateFlow<UserSession> = _currentSession.asStateFlow()

    // Active Student Profile
    private val _studentProfile = MutableStateFlow(defaultStudent)
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    // All registered students (for Teacher/Admin)
    private val _allStudents = MutableStateFlow(sampleStudents)
    val allStudents: StateFlow<List<StudentProfile>> = _allStudents.asStateFlow()

    // Live Location
    private val _currentLocation = MutableStateFlow(
        LocationData(
            latitude = 28.5450,
            longitude = 77.1926,
            address = "Gate 3, Engineering Complex, Hauz Khas, New Delhi - 110016",
            accuracyMeters = 3.8f
        )
    )
    val currentLocation: StateFlow<LocationData> = _currentLocation.asStateFlow()

    // Active Emergency State (null if none active)
    private val _activeEmergency = MutableStateFlow<EmergencyAlert?>(null)
    val activeEmergency: StateFlow<EmergencyAlert?> = _activeEmergency.asStateFlow()

    // Emergency History Log
    private val _emergencyHistory = MutableStateFlow<List<EmergencyAlert>>(
        listOf(
            EmergencyAlert(
                id = "alert_past_01",
                studentId = "stu_001",
                studentName = "Aarav Sharma",
                studentClass = "CS-3A / Roll 42",
                studentPhone = "+91 98765 43210",
                bloodGroup = "O+",
                isHosteller = false,
                alertType = AlertType.NEED_HELP,
                status = EmergencyStatus.RESOLVED,
                location = LocationData(
                    latitude = 28.5492,
                    longitude = 77.1950,
                    address = "Metro Station Exit 2, Hauz Khas, New Delhi"
                ),
                timestamp = System.currentTimeMillis() - (86400000L * 3), // 3 days ago
                remainingMinutes = 0,
                resolutionNotes = "Student verified safe. Parent picked up from Metro Station."
            ),
            EmergencyAlert(
                id = "alert_past_02",
                studentId = "stu_002",
                studentName = "Priya Patel",
                studentClass = "IT-2B / Roll 18",
                studentPhone = "+91 98765 11223",
                bloodGroup = "B+",
                isHosteller = true,
                alertType = AlertType.MEDICAL,
                status = EmergencyStatus.RESOLVED,
                location = LocationData(
                    latitude = 28.5430,
                    longitude = 77.1910,
                    address = "Girls Hostel Block A, Main Campus"
                ),
                timestamp = System.currentTimeMillis() - (86400000L * 7), // 7 days ago
                remainingMinutes = 0,
                resolutionNotes = "First aid administered by campus medical infirmary. Safe."
            )
        )
    )
    val emergencyHistory: StateFlow<List<EmergencyAlert>> = _emergencyHistory.asStateFlow()

    // Geo Safe Zones
    private val _safeZones = MutableStateFlow<List<GeoSafeZone>>(
        listOf(
            GeoSafeZone(
                id = "zone_campus",
                name = "Institute Campus",
                latitude = 28.5450,
                longitude = 77.1926,
                radiusMeters = 600f,
                isInside = true
            ),
            GeoSafeZone(
                id = "zone_home",
                name = "Home (Vasant Kunj)",
                latitude = 28.5245,
                longitude = 77.1550,
                radiusMeters = 250f,
                isInside = false
            ),
            GeoSafeZone(
                id = "zone_hostel",
                name = "Campus Hostel Block",
                latitude = 28.5435,
                longitude = 77.1915,
                radiusMeters = 300f,
                isInside = false
            )
        )
    )
    val safeZones: StateFlow<List<GeoSafeZone>> = _safeZones.asStateFlow()

    // High-Risk Areas identified by the Tactical Map
    private val _highRiskZones = MutableStateFlow<List<HighRiskZone>>(
        listOf(
            HighRiskZone(
                id = "risk_zone_01",
                name = "Unlit Canal Transit Corridor",
                description = "Poor street lighting, low patrol frequency after 8 PM",
                latitude = 28.5468,
                longitude = 77.1952,
                radiusMeters = 500f,
                riskLevel = RiskLevel.HIGH,
                reportedIncidents = 4
            ),
            HighRiskZone(
                id = "risk_zone_02",
                name = "Isolated Ridge Forest Belt",
                description = "Dense unmonitored forested path outside West Campus boundary",
                latitude = 28.5410,
                longitude = 77.1865,
                radiusMeters = 650f,
                riskLevel = RiskLevel.EXTREME,
                reportedIncidents = 7
            ),
            HighRiskZone(
                id = "risk_zone_03",
                name = "Deserted Railway Underpass",
                description = "Isolated underpass with blind turns and dead cell reception zones",
                latitude = 28.5385,
                longitude = 77.1980,
                radiusMeters = 400f,
                riskLevel = RiskLevel.MODERATE,
                reportedIncidents = 2
            )
        )
    )
    val highRiskZones: StateFlow<List<HighRiskZone>> = _highRiskZones.asStateFlow()

    // Log of 'Safe' Status Updates sent to registered contacts
    private val _safeStatusHistory = MutableStateFlow<List<SafeStatusUpdate>>(
        listOf(
            SafeStatusUpdate(
                id = "safe_update_demo",
                studentName = defaultStudent.fullName,
                timestamp = System.currentTimeMillis() - (1800000L * 2), // 1 hour ago
                zoneName = "Unlit Canal Transit Corridor",
                location = _currentLocation.value,
                recipientsCount = 2,
                recipientPhoneList = listOf(defaultStudent.emergencyContact1.phone, defaultStudent.emergencyContact2.phone),
                messageText = "🛡️ SELR SAFE STATUS: Aarav Sharma is currently in monitored high-risk zone 'Unlit Canal Transit Corridor'. Status: ALL SAFE & OK.",
                deliveryStatus = "CONFIRMED DELIVERED TO 2 CONTACTS"
            )
        )
    )
    val safeStatusHistory: StateFlow<List<SafeStatusUpdate>> = _safeStatusHistory.asStateFlow()

    fun addSafeStatusUpdate(update: SafeStatusUpdate) {
        _safeStatusHistory.value = listOf(update) + _safeStatusHistory.value
    }

    fun findActiveHighRiskZone(location: LocationData): HighRiskZone? {
        return _highRiskZones.value.firstOrNull { zone ->
            val dist = calculateDistanceMeters(
                location.latitude,
                location.longitude,
                zone.latitude,
                zone.longitude
            )
            dist <= zone.radiusMeters
        }
    }

    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    fun addHighRiskZone(zone: HighRiskZone) {
        _highRiskZones.value = _highRiskZones.value + zone
    }

    fun removeHighRiskZone(id: String) {
        _highRiskZones.value = _highRiskZones.value.filter { it.id != id }
    }

    // Geofence Events
    private val _geofenceEvents = MutableStateFlow<List<GeofenceEvent>>(
        listOf(
            GeofenceEvent(
                id = "geo_01",
                zoneName = "Institute Campus",
                eventType = "ENTERED",
                timestamp = System.currentTimeMillis() - (3600000L * 4),
                location = _currentLocation.value
            ),
            GeofenceEvent(
                id = "geo_02",
                zoneName = "Home (Vasant Kunj)",
                eventType = "EXITED",
                timestamp = System.currentTimeMillis() - (3600000L * 5),
                location = LocationData(28.5245, 77.1550, "Pocket 4, Vasant Kunj, New Delhi")
            )
        )
    )
    val geofenceEvents: StateFlow<List<GeofenceEvent>> = _geofenceEvents.asStateFlow()

    fun updateLocation(newLocation: LocationData) {
        _currentLocation.value = newLocation
        // If an emergency is active, update its location too
        _activeEmergency.value?.let { currentAlert ->
            _activeEmergency.value = currentAlert.copy(location = newLocation)
        }
    }

    fun updateStudentProfile(updated: StudentProfile) {
        _studentProfile.value = updated
        _allStudents.value = _allStudents.value.map {
            if (it.id == updated.id) updated else it
        }
        if (_currentSession.value.role == UserRole.STUDENT) {
            _currentSession.value = _currentSession.value.copy(displayName = updated.fullName)
        }
    }

    fun registerNewStudent(profile: StudentProfile) {
        _studentProfile.value = profile
        _allStudents.value = listOf(profile) + _allStudents.value.filter { it.id != profile.id }
        _currentSession.value = UserSession(
            userId = "usr_${profile.id}",
            role = UserRole.STUDENT,
            displayName = profile.fullName,
            emailOrPhone = profile.mobile,
            linkedStudentId = profile.id
        )
    }

    fun loginUser(role: UserRole, identifier: String, name: String? = null) {
        val displayName = when (role) {
            UserRole.STUDENT -> _studentProfile.value.fullName
            UserRole.PARENT -> name ?: "Dr. Ramesh Sharma (Parent)"
            UserRole.TEACHER -> name ?: "Prof. Rajesh Kumar (HOD CS)"
            UserRole.ADMIN -> name ?: "Chief Security Officer K. S. Rathore"
        }
        _currentSession.value = UserSession(
            userId = "usr_${UUID.randomUUID().toString().take(6)}",
            role = role,
            displayName = displayName,
            emailOrPhone = identifier,
            linkedStudentId = if (role == UserRole.PARENT || role == UserRole.STUDENT) _studentProfile.value.id else null
        )
    }

    fun triggerEmergency(type: AlertType, customSmsNote: String = ""): EmergencyAlert {
        val student = _studentProfile.value
        val loc = _currentLocation.value
        val alert = EmergencyAlert(
            id = "alert_${System.currentTimeMillis()}",
            studentId = student.id,
            studentName = student.fullName,
            studentClass = student.classRollNo,
            studentPhone = student.mobile,
            bloodGroup = student.bloodGroup,
            isHosteller = student.isHosteller,
            alertType = type,
            status = EmergencyStatus.ACTIVE,
            location = loc,
            timestamp = System.currentTimeMillis(),
            remainingMinutes = 60,
            parentNotified = true,
            teacherNotified = true,
            adminNotified = true,
            smsSentLog = customSmsNote
        )
        _activeEmergency.value = alert
        return alert
    }

    fun cancelActiveEmergency(reason: String) {
        _activeEmergency.value?.let { alert ->
            val cancelledAlert = alert.copy(
                status = EmergencyStatus.CANCELLED,
                resolutionNotes = "Cancelled by student. Reason: $reason"
            )
            _emergencyHistory.value = listOf(cancelledAlert) + _emergencyHistory.value
        }
        _activeEmergency.value = null
    }

    fun resolveEmergency(alertId: String, notes: String) {
        if (_activeEmergency.value?.id == alertId) {
            val resolved = _activeEmergency.value!!.copy(
                status = EmergencyStatus.RESOLVED,
                resolutionNotes = notes
            )
            _emergencyHistory.value = listOf(resolved) + _emergencyHistory.value
            _activeEmergency.value = null
        } else {
            _emergencyHistory.value = _emergencyHistory.value.map {
                if (it.id == alertId) it.copy(status = EmergencyStatus.RESOLVED, resolutionNotes = notes) else it
            }
        }
    }

    fun addSafeZone(name: String, radius: Float) {
        val loc = _currentLocation.value
        val newZone = GeoSafeZone(
            id = "zone_${UUID.randomUUID().toString().take(6)}",
            name = name,
            latitude = loc.latitude,
            longitude = loc.longitude,
            radiusMeters = radius,
            isInside = true
        )
        _safeZones.value = _safeZones.value + newZone
    }

    fun removeSafeZone(zoneId: String) {
        _safeZones.value = _safeZones.value.filter { it.id != zoneId }
    }
}
