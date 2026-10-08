package com.example.model

enum class UserRole(val displayName: String, val badge: String) {
    STUDENT("Student", "STUDENT DEFENSE"),
    PARENT("Parent / Guardian", "PARENT WATCH"),
    TEACHER("Teacher / Faculty", "FACULTY DISPATCH"),
    ADMIN("Campus Security / Admin", "COMMAND CENTER")
}

enum class AlertType(val title: String, val emoji: String) {
    SOS_CRITICAL("CRITICAL EMERGENCY SOS", "🚨"),
    MEDICAL("MEDICAL ASSISTANCE", "🚑"),
    NEED_HELP("DISCREET HELP ALERT", "⚠️"),
    GEOFENCE_BREACH("SAFE ZONE BREACH", "📍")
}

enum class EmergencyStatus(val label: String) {
    ACTIVE("ACTIVE RESCUE"),
    RESOLVED("RESOLVED"),
    CANCELLED("CANCELLED BY USER")
}

data class EmergencyContact(
    val name: String,
    val relation: String,
    val phone: String
)

data class StudentProfile(
    val id: String,
    val fullName: String,
    val classRollNo: String,
    val schoolCollegeName: String,
    val bloodGroup: String,
    val isHosteller: Boolean, // true = Hosteller, false = Day-Scholar
    val mobile: String,
    val emergencyContact1: EmergencyContact,
    val emergencyContact2: EmergencyContact,
    val medicalNotes: String = "No known drug allergies. Asthmatic inhaler in bag.",
    val avatarInitial: String = "S"
)

data class UserSession(
    val userId: String,
    val role: UserRole,
    val displayName: String,
    val emailOrPhone: String,
    val organization: String = "National Defense & Technology Institute",
    val linkedStudentId: String? = null
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val accuracyMeters: Float = 4.2f,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val googleMapsUrl: String
        get() = "https://maps.google.com/?q=$latitude,$longitude"
}

data class EmergencyAlert(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentClass: String,
    val studentPhone: String,
    val bloodGroup: String,
    val isHosteller: Boolean,
    val alertType: AlertType,
    val status: EmergencyStatus,
    val location: LocationData,
    val timestamp: Long = System.currentTimeMillis(),
    val remainingMinutes: Int = 60,
    val parentNotified: Boolean = true,
    val teacherNotified: Boolean = true,
    val adminNotified: Boolean = true,
    val smsSentLog: String = "",
    val resolutionNotes: String? = null
)

data class GeoSafeZone(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val isInside: Boolean = true
)

data class GeofenceEvent(
    val id: String,
    val zoneName: String,
    val eventType: String, // "ENTERED" or "EXITED"
    val timestamp: Long = System.currentTimeMillis(),
    val location: LocationData
)

data class DeviceStatus(
    val batteryPercent: Int = 87,
    val isCharging: Boolean = false,
    val signalStrength: String = "Strong 5G",
    val gpsStatus: String = "High Accuracy GPS Locked (4m)"
)

data class FakeCallState(
    val isRinging: Boolean = false,
    val isInCall: Boolean = false,
    val callerName: String = "Dad (Home)",
    val callerNumber: String = "+91 98765 43210",
    val callDurationSeconds: Int = 0
)

data class NearbyContactLocation(
    val id: String,
    val name: String,
    val relation: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Float,
    val etaMinutes: Int,
    val bearingDegrees: Float,
    val status: String = "En Route"
)

enum class RiskLevel(val label: String, val badgeColor: Long) {
    MODERATE("MODERATE CAUTION", 0xFFFF9800),
    HIGH("HIGH RISK SECTOR", 0xFFE53935),
    EXTREME("CRITICAL DANGER ZONE", 0xFFB71C1C)
}

data class HighRiskZone(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 500f,
    val riskLevel: RiskLevel = RiskLevel.HIGH,
    val isStudentInside: Boolean = false,
    val reportedIncidents: Int = 3
)

data class SafeStatusUpdate(
    val id: String,
    val studentName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val zoneName: String,
    val location: LocationData,
    val recipientsCount: Int,
    val recipientPhoneList: List<String>,
    val messageText: String,
    val deliveryStatus: String = "DELIVERED TO ALL CONTACTS"
)

data class HighRiskMonitoringState(
    val isInsideHighRiskArea: Boolean = false,
    val activeZone: HighRiskZone? = null,
    val nextUpdateSecondsRemaining: Int = 30 * 60, // 30 minutes (1800s)
    val totalUpdatesSent: Int = 0,
    val lastUpdateTimestamp: Long? = null,
    val isAutoUpdatesEnabled: Boolean = true,
    val isFastTestInterval: Boolean = false // Allows 30s demo test mode for demonstration
)
