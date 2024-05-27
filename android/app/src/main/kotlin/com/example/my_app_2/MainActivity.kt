package com.example.my_app_2

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.annotation.NonNull


class MainActivity: FlutterActivity(){
    private val CHANNEL = "com.yourcompany.yourapp/appblocker"

    private val OVERLAY_PERMISSION_REQ_CODE = 1  // Choose an arbitrary integer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this@MainActivity)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            }
        }

        if (!hasUsageStatsPermission(this@MainActivity)) {
            requestUsageStatsPermission(this@MainActivity)
        }
    }

    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        } else {
            appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startService" -> {
                    val packageNameList = call.argument<List<String>>("packageName")
                    val packageNameArrayList = ArrayList(packageNameList) // Convert the List to an ArrayList

                    val duration = call.argument<Int>("duration")?.toLong() ?: 0L
                    val intent = Intent(this, AppBlockerService::class.java)
                    intent.putExtra("BLOCK_DURATION", duration)
                    intent.putExtra("BLOCK_APPS", packageNameArrayList)
                    startService(intent)
                    result.success(null)
                }
                "stopService" -> {
                    val packageName = call.argument<String>("packageName")
                    val intent = Intent(this, AppBlockerService::class.java)
                    intent.putExtra("STOP_PACKAGE", packageName)
                    stopService(intent)
                    result.success(null)
                }
//                "stopService" -> {
//                    val intent = Intent(this, AppBlockerService::class.java)
//                    stopService(intent)
//                    result.success(null)
//                }
                else -> result.notImplemented()
            }
        }
    }
}
