package com.example.t1_leairn

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val METHOD_CHANNEL_NAME = "app_opener"
    private var methodChannel: MethodChannel? = null
    private val handler = Handler(Looper.getMainLooper())
    private var targetPackages = setOf<String>()

    companion object {
        const val REQUEST_CODE = 1234
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, METHOD_CHANNEL_NAME)

        methodChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "updateBlockedApps" -> {
                    val appsList = call.arguments as List<String>
                    targetPackages = appsList.map { getPackageNameForApp(it) }.toSet()
                    Log.d("AppBlocker", "Updated list of blocked apps: $targetPackages")
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        }

        startPeriodicInvocation()
    }

    private fun startPeriodicInvocation() {
        val TAG = "AppUsageService"
        var loopCount = 0
        var switchSceneCooldown = -2

        Thread {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            Log.d(TAG, "Thread started: Monitoring app usage")

            while (true) {
                try {
                    loopCount++
                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - 1000

                    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
                    val event = UsageEvents.Event()

                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event)
                        Log.d(TAG, "Event detected: ${event.eventType}, on: ${event.packageName}")

                        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            if (targetPackages.contains(event.packageName)) {
                                Log.d(TAG, "Target app (${event.packageName}) opened")

                                if (switchSceneCooldown <= 0) {
                                    switchSceneCooldown = 10
                                    handler.post {
                                        moveTaskToBack(true)

                                        val intent = Intent(this@MainActivity, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                Log.d(TAG, "Other app opened: ${event.packageName}")
                            }
                        }
                    }
                    if (switchSceneCooldown > 0) {
                        switchSceneCooldown--
                        Log.d(TAG, "Switch Scene Cooldown: $switchSceneCooldown")
                    } else if (switchSceneCooldown == 0) {
                        switchSceneCooldown = -2
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in usage monitoring loop", e)
                }

                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread interrupted", e)
                }
            }
        }.start()
    }

    private fun getPackageNameForApp(appName: String): String {
        return when (appName) {
            "Instagram" -> "com.instagram.android"
            "Youtube" -> "com.google.android.youtube"
            "Chrome" -> "com.android.chrome"
            else -> ""
        }
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

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
