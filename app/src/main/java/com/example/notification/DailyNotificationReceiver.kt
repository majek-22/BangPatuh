package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DailyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("DailyNotifReceiver", "7:30 AM daily alarm triggered!")
        val todayMessage = DailyComplianceRepository.getTodayMessage()
        DailyNotificationScheduler.showNotification(context, todayMessage)

        // Reschedule for 7:30 AM the next day
        DailyNotificationScheduler.scheduleDaily730AmAlarm(context)
    }
}
