package com.example.t1_leairn

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val METHOD_CHANNEL_NAME = "app_opener"
    private var methodChannel: MethodChannel? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, METHOD_CHANNEL_NAME)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        }

        startPeriodicInvocation()
    }

    private fun startPeriodicInvocation() {
        val TAG = "AppUsageService"
        val TARGET_APP_PACKAGE = "com.instagram.android"
        var loopCount = 0
        var switchSceneCooldown = 0

        Thread {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            Log.d(TAG, "Thread started: Monitoring app usage")

            while (true) {
                try {
                    loopCount++
                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - 1000 * 1 // check every 1s
                    // Log.d(TAG, "Thread loop: $loopCount")

                    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
                    val event = UsageEvents.Event()
                    
                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event)
                        Log.d(TAG, "Event detected: ${event.eventType}, on: ${event.packageName}")

                        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            if (TARGET_APP_PACKAGE == event.packageName) {
                                Log.d(TAG, "Target app ($TARGET_APP_PACKAGE) opened")
                                
                                // Post the method call to the main thread
                                if (switchSceneCooldown <= 0 ) {
                                    switchSceneCooldown = 60
                                    handler.post {
                                        methodChannel?.invokeMethod("switch_scene", null, object : MethodChannel.Result {
                                            override fun success(result: Any?) {
                                                println("Scene switch triggered from Kotlin and succeeded")
                                            }

                                            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                                println("Error: $errorCode, $errorMessage")
                                            }

                                            override fun notImplemented() {
                                                println("Method not implemented")
                                            }
                                        })
                                    }
                                }
                            } else {
                                Log.d(TAG, "Other app opened: ${event.packageName}")
                            }
                        }
                    }
                    switchSceneCooldown--
                    Log.d(TAG, "Switch Scene Cooldown: $switchSceneCooldown")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in usage monitoring loop", e)
                }

                try {
                    Thread.sleep(1000) // check every 1s
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread interrupted", e)
                }
            }
        }.start()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
        return stats.isNotEmpty()
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}
