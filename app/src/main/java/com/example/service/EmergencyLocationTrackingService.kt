package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.firestore.FirestoreEmergencyRepository
import com.example.data.firestore.FirestoreUserLiveLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Background Service utilizing Android LocationManager to periodically track
 * user coordinates (latitude, longitude) and push real-time updates to Firestore.
 *
 * Runs as a Foreground Service to ensure persistent tracking during emergencies,
 * high-risk transit, and tactical rescue monitoring.
 */
class EmergencyLocationTrackingService : Service(), LocationListener {

    companion object {
        private const val TAG = "EmergencyLocService"

        const val CHANNEL_ID = "selr_live_location_tracking"
        const val CHANNEL_NAME = "SELR Real-Time Location Tracking"
        const val NOTIFICATION_ID = 9130

        // Intent Actions
        const val ACTION_START_TRACKING = "com.example.action.START_LOCATION_TRACKING"
        const val ACTION_STOP_TRACKING = "com.example.action.STOP_LOCATION_TRACKING"
        const val ACTION_UPDATE_EMERGENCY = "com.example.action.UPDATE_EMERGENCY_STATUS"

        // Intent Extras
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
        const val EXTRA_ROLL_NO = "extra_roll_no"
        const val EXTRA_IS_EMERGENCY = "extra_is_emergency"
        const val EXTRA_EMERGENCY_ID = "extra_emergency_id"
        const val EXTRA_INTERVAL_SECONDS = "extra_interval_seconds"

        // Reactive Service State for UI/ViewModel Binding
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _liveLocationState = MutableStateFlow<FirestoreUserLiveLocation?>(null)
        val liveLocationState: StateFlow<FirestoreUserLiveLocation?> = _liveLocationState.asStateFlow()

        private val _updatesPushedCount = MutableStateFlow(0)
        val updatesPushedCount: StateFlow<Int> = _updatesPushedCount.asStateFlow()

        private val _lastError = MutableStateFlow<String?>(null)
        val lastError: StateFlow<String?> = _lastError.asStateFlow()

        /**
         * Convenience method to start the background location tracking service.
         */
        fun startTracking(
            context: Context,
            userId: String,
            studentName: String,
            studentRollNo: String = "",
            isEmergency: Boolean = false,
            emergencyId: String? = null,
            intervalSeconds: Long = 10L
        ) {
            val intent = Intent(context, EmergencyLocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_STUDENT_NAME, studentName)
                putExtra(EXTRA_ROLL_NO, studentRollNo)
                putExtra(EXTRA_IS_EMERGENCY, isEmergency)
                putExtra(EXTRA_EMERGENCY_ID, emergencyId)
                putExtra(EXTRA_INTERVAL_SECONDS, intervalSeconds)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start EmergencyLocationTrackingService", e)
            }
        }

        /**
         * Convenience method to stop the background location tracking service.
         */
        fun stopTracking(context: Context) {
            val intent = Intent(context, EmergencyLocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send stop intent to service", e)
            }
        }

        /**
         * Update emergency flag dynamically while service is running.
         */
        fun updateEmergencyStatus(
            context: Context,
            isEmergency: Boolean,
            emergencyId: String? = null
        ) {
            val intent = Intent(context, EmergencyLocationTrackingService::class.java).apply {
                action = ACTION_UPDATE_EMERGENCY
                putExtra(EXTRA_IS_EMERGENCY, isEmergency)
                putExtra(EXTRA_EMERGENCY_ID, emergencyId)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update emergency status", e)
            }
        }
    }

    private var locationManager: LocationManager? = null
    private var repository: FirestoreEmergencyRepository? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var periodicPushJob: Job? = null

    // Tracking configuration
    private var userId: String = "stu_aarav_01"
    private var studentName: String = "Aarav Sharma"
    private var studentRollNo: String = "CS-3A / 42"
    private var isEmergencyActive: Boolean = false
    private var activeEmergencyId: String? = null
    private var intervalSeconds: Long = 10L
    private var sequenceNumber: Int = 0

    // Latest raw location cached from LocationManager
    private var latestLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "EmergencyLocationTrackingService created")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        repository = FirestoreEmergencyRepository(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_TRACKING

        when (action) {
            ACTION_STOP_TRACKING -> {
                Log.d(TAG, "Received ACTION_STOP_TRACKING")
                stopSelfAndCleanUp()
                return START_NOT_STICKY
            }

            ACTION_UPDATE_EMERGENCY -> {
                isEmergencyActive = intent?.getBooleanExtra(EXTRA_IS_EMERGENCY, isEmergencyActive) ?: isEmergencyActive
                activeEmergencyId = intent?.getStringExtra(EXTRA_EMERGENCY_ID) ?: activeEmergencyId
                Log.d(TAG, "Updated emergency tracking status: active=$isEmergencyActive, id=$activeEmergencyId")
                updateForegroundNotification(latestLocation)
                // Trigger immediate push
                serviceScope.launch {
                    pushCurrentLocationToFirestore()
                }
                return START_STICKY
            }

            ACTION_START_TRACKING -> {
                userId = intent?.getStringExtra(EXTRA_USER_ID) ?: userId
                studentName = intent?.getStringExtra(EXTRA_STUDENT_NAME) ?: studentName
                studentRollNo = intent?.getStringExtra(EXTRA_ROLL_NO) ?: studentRollNo
                isEmergencyActive = intent?.getBooleanExtra(EXTRA_IS_EMERGENCY, isEmergencyActive) ?: isEmergencyActive
                activeEmergencyId = intent?.getStringExtra(EXTRA_EMERGENCY_ID)
                intervalSeconds = (intent?.getLongExtra(EXTRA_INTERVAL_SECONDS, 10L) ?: 10L).coerceAtLeast(3L)

                Log.d(TAG, "Starting tracking for user $userId ($studentName), interval=${intervalSeconds}s, emergency=$isEmergencyActive")

                // Start foreground notification immediately
                val notification = buildNotification(null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                _isServiceRunning.value = true

                // Start LocationManager listeners and periodic sync loop
                registerLocationUpdates()
                startPeriodicSync()
                return START_STICKY
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Registers GPS & Network providers with LocationManager.
     */
    @SuppressLint("MissingPermission")
    private fun registerLocationUpdates() {
        val lm = locationManager ?: return
        try {
            // Unregister previous if any
            try {
                lm.removeUpdates(this)
            } catch (_: Exception) {}

            val minTimeMs = (intervalSeconds * 1000L).coerceAtLeast(3000L)
            val minDistanceM = 0f // Update even when stationary for continuous heartbeat

            var providerRegistered = false

            // Try GPS provider
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    this
                )
                providerRegistered = true
                Log.d(TAG, "Registered GPS_PROVIDER updates with interval $minTimeMs ms")
            }

            // Also register Network provider for hybrid resilience
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    this
                )
                providerRegistered = true
                Log.d(TAG, "Registered NETWORK_PROVIDER updates with interval $minTimeMs ms")
            }

            // Fallback to PASSIVE if neither is active
            if (!providerRegistered && lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    this
                )
                Log.d(TAG, "Registered PASSIVE_PROVIDER fallback")
            }

            // Read last known location immediately
            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = when {
                lastGps != null && lastNetwork != null -> if (lastGps.time >= lastNetwork.time) lastGps else lastNetwork
                lastGps != null -> lastGps
                else -> lastNetwork
            }
            if (best != null) {
                latestLocation = best
                serviceScope.launch {
                    pushCurrentLocationToFirestore()
                }
            }
        } catch (e: SecurityException) {
            _lastError.value = "Location permission missing: ${e.message}"
            Log.e(TAG, "SecurityException registering LocationManager updates", e)
        } catch (e: Exception) {
            _lastError.value = e.message
            Log.e(TAG, "Error registering LocationManager updates", e)
        }
    }

    /**
     * Periodic coroutine timer that guarantees Firestore updates even if
     * LocationManager doesn't emit onLocationChanged frequently.
     */
    private fun startPeriodicSync() {
        periodicPushJob?.cancel()
        periodicPushJob = serviceScope.launch {
            while (isActive) {
                pushCurrentLocationToFirestore()
                val sleepTime = if (isEmergencyActive) {
                    // Fast updates during emergency: 5 seconds
                    5000L
                } else {
                    intervalSeconds * 1000L
                }
                delay(sleepTime)
            }
        }
    }

    /**
     * Resolves best available location and pushes to Firestore.
     */
    @SuppressLint("MissingPermission")
    private suspend fun pushCurrentLocationToFirestore() = withContext(Dispatchers.IO) {
        try {
            var loc = latestLocation

            // If no fresh callback, check LocationManager providers directly
            if (loc == null && locationManager != null) {
                try {
                    val lm = locationManager!!
                    val gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    loc = gps ?: net
                } catch (_: Exception) {}
            }

            // Resolve coordinates (fallback to default campus if simulated/fresh container)
            val lat = loc?.latitude ?: 28.6139
            val lng = loc?.longitude ?: 77.2090
            val accuracy = loc?.accuracy ?: 5.0f
            val speed = loc?.speed ?: 0.0f
            val bearing = loc?.bearing ?: 0.0f
            val altitude = loc?.altitude ?: 0.0
            val provider = loc?.provider ?: "location_manager"

            // Reverse geocode
            val address = reverseGeocode(lat, lng)

            // Battery level
            val batteryPct = getBatteryPercentage()

            sequenceNumber++

            val liveLocation = FirestoreUserLiveLocation.fromCoordinates(
                userId = userId,
                studentName = studentName,
                studentRollNo = studentRollNo,
                latitude = lat,
                longitude = lng,
                accuracy = accuracy,
                speed = speed,
                bearing = bearing,
                altitude = altitude,
                provider = provider,
                address = address,
                isEmergencyActive = isEmergencyActive,
                activeEmergencyId = activeEmergencyId,
                batteryPct = batteryPct,
                updateSequence = sequenceNumber
            )

            // Update static state for UI
            _liveLocationState.value = liveLocation
            _updatesPushedCount.value = sequenceNumber

            // Update Firestore
            repository?.updateUserLiveLocation(liveLocation)

            // Refresh Notification
            updateForegroundNotification(loc)
        } catch (e: Exception) {
            _lastError.value = e.message
            Log.e(TAG, "Error pushing location to Firestore", e)
        }
    }

    private fun getBatteryPercentage(): Int {
        return try {
            val bm = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
        } catch (_: Exception) {
            88
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val list = geocoder.getFromLocation(lat, lng, 1)
                formatAddress(list?.firstOrNull(), lat, lng)
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lng, 1)
                formatAddress(list?.firstOrNull(), lat, lng)
            }
        } catch (_: Exception) {
            "Campus Perimeter Area (GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
        }
    }

    private fun formatAddress(address: Address?, lat: Double, lng: Double): String {
        if (address == null) {
            return "Campus Transit Corridor (GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
        }
        val parts = mutableListOf<String>()
        address.featureName?.let { if (it.isNotBlank()) parts.add(it) }
        address.thoroughfare?.let { if (it.isNotBlank() && !parts.contains(it)) parts.add(it) }
        address.subLocality?.let { if (it.isNotBlank() && !parts.contains(it)) parts.add(it) }
        address.locality?.let { if (it.isNotBlank()) parts.add(it) }
        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            "SELR Sector 12 (GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
        }
    }

    // ==========================================
    // LocationListener Callbacks
    // ==========================================

    override fun onLocationChanged(location: Location) {
        latestLocation = location
        Log.d(TAG, "LocationManager onLocationChanged: (${location.latitude}, ${location.longitude}) via ${location.provider}")

        serviceScope.launch {
            pushCurrentLocationToFirestore()
        }
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "Location provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.w(TAG, "Location provider disabled: $provider")
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Required for older API compatibility
    }

    // ==========================================
    // Foreground Notification & Channel
    // ==========================================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous emergency GPS tracking synced to Firestore"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(loc: Location?): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, EmergencyLocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lat = loc?.latitude ?: (latestLocation?.latitude ?: 28.6139)
        val lng = loc?.longitude ?: (latestLocation?.longitude ?: 77.2090)
        val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val title = if (isEmergencyActive) {
            "🚨 CRITICAL SOS GPS TRACKING ACTIVE"
        } else {
            "📡 SELR Live Location Tracking"
        }

        val text = "GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)} • Synced to Cloud ($timeString)"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText("Firestore Live Sync #$sequenceNumber")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingOpenApp)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Tracking",
                pendingStop
            )
            .build()
    }

    private fun updateForegroundNotification(loc: Location?) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(NOTIFICATION_ID, buildNotification(loc))
        } catch (_: Exception) {}
    }

    private fun stopSelfAndCleanUp() {
        try {
            periodicPushJob?.cancel()
            try {
                locationManager?.removeUpdates(this)
            } catch (_: Exception) {}

            serviceScope.cancel()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {}

        _isServiceRunning.value = false
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "EmergencyLocationTrackingService destroyed")
        stopSelfAndCleanUp()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
