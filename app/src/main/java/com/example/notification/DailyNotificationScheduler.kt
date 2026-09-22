package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object DailyNotificationScheduler {

    private const val TAG = "DailyNotifScheduler"
    const val CHANNEL_ID = "compliance_daily_briefing"
    const val NOTIFICATION_ID = 730
    const val REQUEST_CODE_ALARM = 7301
    const val EXTRA_OPEN_DAILY_NOTIF = "extra_open_daily_notif"
    const val EXTRA_MESSAGE_ID = "extra_message_id"

    /**
     * Initializes notification channels and schedules the 07:30 AM daily alarm.
     */
    fun initNotificationChannelAndSchedule(context: Context) {
        createNotificationChannel(context)
        scheduleDaily730AmAlarm(context)
    }

    /**
     * Creates the Android 8.0+ notification channel.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Edukasi Kepatuhan Harian (07:30 AM)"
            val descriptionText = "Peringatan harian pencegahan fraud, anti-pencucian uang, dan integritas korporasi."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules an alarm to fire every day at 07:30 AM.
     */
    fun scheduleDaily730AmAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, DailyNotificationReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALARM,
            intent,
            flags
        )

        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If 7:30 AM has already passed today, target 7:30 AM tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetCal.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetCal.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled 7:30 AM compliance notification for: ${targetCal.time}")
        } catch (e: SecurityException) {
            // In Android 12+ if SCHEDULE_EXACT_ALARM is restricted, fallback to inexact set()
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetCal.timeInMillis,
                pendingIntent
            )
            Log.w(TAG, "Fallback to inexact alarm due to security exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling daily alarm: ${e.message}")
        }
    }

    /**
     * Triggers the notification immediately for testing or previewing.
     */
    fun triggerNotificationNow(context: Context) {
        createNotificationChannel(context)
        val todayMessage = DailyComplianceRepository.getTodayMessage()
        showNotification(context, todayMessage)
    }

    /**
     * Builds and sends the system notification.
     */
    fun showNotification(context: Context, message: DailyComplianceMessage) {
        createNotificationChannel(context)

        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_DAILY_NOTIF, true)
            putExtra(EXTRA_MESSAGE_ID, message.id)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.id,
            clickIntent,
            flags
        )

        // Read language from SharedPreferences
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("selected_language", "id") ?: "id"

        val title = "${message.getLocalizedBadge(currentLang)}: ${message.getLocalizedTitle(currentLang)}"
        val summary = message.getLocalizedPushSummary(currentLang)

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, message.iconRes)
        } catch (_: Exception) {
            null
        }

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .bigText(
                summary + "\n\n💡 " + (message.getLocalizedGoldenRules(currentLang).firstOrNull() ?: "")
            )
            .setSummaryText("Pesan Kepatuhan 07:30 Pagi")

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_laurel_shield)
            .setContentTitle(title)
            .setContentText(summary)
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)

        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            Log.d(TAG, "Daily notification posted successfully for message: ${message.id}")
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission missing: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification: ${e.message}")
        }
    }
}
