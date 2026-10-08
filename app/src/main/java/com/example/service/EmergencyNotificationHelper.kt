package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.EmergencyAlert

class EmergencyNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
        const val CHANNEL_ID = "selr_emergency_dispatch"
        const val CHANNEL_NAME = "SELR Emergency SOS Alerts"
        const val NOTIFICATION_ID = 9110

        const val CHANNEL_SAFE_ID = "selr_safe_status_updates"
        const val CHANNEL_SAFE_NAME = "SELR High-Risk Safe Updates"
        const val SAFE_NOTIFICATION_ID = 9120
    }

    init {
        try {
            createNotificationChannels()
        } catch (_: Exception) {}
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val emergencyChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for student emergency location and rescue"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 800)
            }
            notificationManager?.createNotificationChannel(emergencyChannel)

            val safeChannel = NotificationChannel(
                CHANNEL_SAFE_ID,
                CHANNEL_SAFE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic Safe status update broadcasts when inside high-risk zones"
                enableVibration(false)
            }
            notificationManager?.createNotificationChannel(safeChannel)
        }
    }

    fun showEmergencyNotification(alert: EmergencyAlert) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smsBody = formatEmergencySms(alert)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 SELR CRITICAL EMERGENCY: ${alert.studentName}")
            .setContentText("Class ${alert.studentClass} needs help! Live GPS tracking active.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(smsBody)
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)

        try {
            notificationManager?.notify(NOTIFICATION_ID, builder.build())
        } catch (_: Exception) {}
    }

    fun cancelEmergencyNotification() {
        try {
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    fun formatEmergencySms(alert: EmergencyAlert): String {
        return "🚨 SELR EMERGENCY! ${alert.studentName} Class ${alert.studentClass} needs help at Location: ${alert.location.googleMapsUrl} - ${alert.location.address}. Live tracking active."
    }

    fun showSafeStatusNotification(
        studentName: String,
        zoneName: String,
        address: String,
        contactsCount: Int,
        nextIntervalMinutes: Int = 30
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summaryText = "Periodic 'Safe' broadcast sent to $contactsCount registered emergency contacts. Zone: $zoneName. Next update in ${nextIntervalMinutes}m."

        val builder = NotificationCompat.Builder(context, CHANNEL_SAFE_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🛡️ SELR 'Safe' Status Broadcasted ($zoneName)")
            .setContentText("Status: ALL SAFE & OK. Dispatched to $contactsCount contacts.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$summaryText\nLocation: $address")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager?.notify(SAFE_NOTIFICATION_ID, builder.build())
        } catch (_: Exception) {}
    }

    fun formatSafeStatusSms(
        studentName: String,
        studentClass: String,
        zoneName: String,
        location: com.example.model.LocationData,
        nextIntervalMinutes: Int = 30
    ): String {
        return "🛡️ SELR SAFE STATUS: $studentName ($studentClass) is currently in monitored high-risk zone '$zoneName' at ${location.address}. Status: ALL SAFE & OK. Periodic 30-min auto check-in active. Next update in ${nextIntervalMinutes} mins. GPS: ${location.googleMapsUrl}"
    }

    fun sendEmergencySmsIntent(phoneNumbers: List<String>, message: String) {
        val uri = Uri.parse("smsto:${phoneNumbers.joinToString(";")}")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Intent couldn't be resolved, fallback handled in UI
        }
    }

    fun openMapsNavigation(latitude: Double, longitude: Double) {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(SELR Emergency Student Location)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val browserUri = Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    fun makePhoneCall(phoneNumber: String) {
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
