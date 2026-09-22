package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "Re-scheduling daily 7:30 AM compliance alarm after: ${intent?.action}")
        DailyNotificationScheduler.initNotificationChannelAndSchedule(context)
    }
}
