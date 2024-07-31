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
    private var loopCount = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service created")

        createNotificationChannel()
        Log.d(TAG, "onCreate: Notification channel created")

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("App Usage Service")
            .setContentText("Monitoring app usage")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        
        startForeground(1, notification)
        Log.d(TAG, "onCreate: Service started in foreground")

        Thread {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            Log.d(TAG, "Thread started: Monitoring app usage")

            while (true) {
                try {
                    loopCount++
                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - 1000 * 1 // check every 10s
                    Log.d(TAG, "Thread loop: $loopCount")

                    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
                    val event = UsageEvents.Event()
                    
                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event)
                        Log.d(TAG, "Event detected: ${event.eventType}, on: ${event.packageName}")

                        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            if (TARGET_APP_PACKAGE == event.packageName) {
                                Log.d(TAG, "Target app ($TARGET_APP_PACKAGE) opened")
                                
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    putExtra("open_practice_page", true)
                                }
                                Log.d(TAG, "Starting MainActivity with intent")
                                startActivity(intent)
                            } else {
                                Log.d(TAG, "Other app opened: ${event.packageName}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in usage monitoring loop", e)
                }

                try {
                    Thread.sleep(1000) // check every 10s
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread interrupted", e)
                }
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel")
            
            val name = "App Usage Service Channel"
            val descriptionText = "Channel for App Usage Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $name")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind called")
        return null
    }
}
