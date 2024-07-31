// File path: t1_leairn/android/app/src/main/kotlin/com/example/t1_leairn/AppUsageService.kt

package com.example.t1_leairn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

class AppUsageService : Service() {

    private val TAG = "AppUsageService"
    private val TARGET_APP_PACKAGE = "com.instagram.android"
    private val CHANNEL_ID = "AppUsageServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("App Usage Service")
            .setContentText("Monitoring app usage")
            .setSmallIcon(R.drawable.ic_notification) // Ensure this drawable exists
            .build())

        Thread {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            while (true) {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - 1000  // check every 1 second
                val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
                val event = UsageEvents.Event()
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        if (TARGET_APP_PACKAGE == event.packageName) {
                            Log.d(TAG, "Target app opened")
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra("open_practice_page", true)
                            startActivity(intent)
                        }
                    }
                }
                Thread.sleep(1000) // check every 10 seconds
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Usage Service Channel"
            val descriptionText = "Channel for App Usage Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
