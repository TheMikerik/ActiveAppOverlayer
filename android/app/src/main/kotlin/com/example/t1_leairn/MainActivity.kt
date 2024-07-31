package com.example.t1_leairn

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.util.Log

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.t1_leairn/intent"
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Log.d(TAG, "onCreate: Starting AppUsageService")
        
        // Start the AppUsageService
        startService(Intent(this, AppUsageService::class.java))

        // Check and request usage stats permission
        if (!hasUsageStatsPermission()) {
            // Log.d(TAG, "onCreate: No usage stats permission. Requesting permission.")
            requestUsageStatsPermission()
        } else {
            // Log.d(TAG, "onCreate: Usage stats permission already granted.")
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        // Log.d(TAG, "configureFlutterEngine: Setting up MethodChannel")

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "getIntentData") {
                // Log.d(TAG, "MethodCallHandler: Handling 'getIntentData' method call")
                
                val intent = intent
                val openPracticePage = intent.getBooleanExtra("open_practice_page", false)
                // Log.d(TAG, "MethodCallHandler: openPracticePage = $openPracticePage")
                
                result.success(openPracticePage)
            } else {
                // Log.d(TAG, "MethodCallHandler: Method '${call.method}' not implemented")
                result.notImplemented()
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
        val hasPermission = stats.isNotEmpty()
        // Log.d(TAG, "hasUsageStatsPermission: Permission status = $hasPermission")
        return hasPermission
    }

    private fun requestUsageStatsPermission() {
        // Log.d(TAG, "requestUsageStatsPermission: Redirecting to usage access settings")
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}
