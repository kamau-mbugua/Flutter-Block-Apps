package com.example.my_app_2

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.view.WindowManager
import android.widget.TextView
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import com.example.my_app_2.R
import java.util.concurrent.TimeUnit

class AppBlockerService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayLayout: LinearLayout

    private val handler = Handler()
    private lateinit var blockedApps: Set<String>
    private var stopHandler: Handler? = null
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "App Blocker Service"

    private var blockStartTime: Long = 0L
    private var blockDuration: Long = 0L

    private lateinit var countdownText: TextView
    private var countdownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

//        blockedApps  = setOf("com.example.my_app_2", "com.google.android.youtube", "com.zhiliaoapp.musically")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayLayout = LinearLayout(this)
        overlayLayout.orientation = LinearLayout.VERTICAL
        overlayLayout.gravity = Gravity.CENTER
        overlayLayout.setBackgroundColor(0x7F000000)

        val overlayImage = ImageView(this)
        overlayImage.setImageResource(R.mipmap.ic_launcher)
        overlayLayout.addView(overlayImage)

        countdownText = TextView(this)
        countdownText.setTextColor(0xFFFFFFFF.toInt())
        countdownText.textSize = 24f
        countdownText.gravity = Gravity.CENTER
        overlayLayout.addView(countdownText)

        val overlayText = TextView(this)
        overlayText.text = "App Blocked, Take a Break!"
//        overlayText.setBackgroundColor(0x7F000000)
        overlayText.setTextColor(0xFFFFFFFF.toInt())
        overlayText.textSize = 24f
        overlayText.gravity = Gravity.CENTER
        overlayLayout.addView(overlayText)

        // Add a Button to the overlayLayout
        val stopBlockingButton = Button(this)
        stopBlockingButton.text = "Stop Blocking"
        stopBlockingButton.setOnClickListener {
            // Stop the service when the button is clicked
            stopSelf()
        }
        overlayLayout.addView(stopBlockingButton)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayLayout, params)

        handler.post(checkForegroundApp)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Blocker Service"
            val descriptionText = "This channel is used by the App Blocker Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val blockedAppList = intent?.getStringArrayListExtra("BLOCK_APPS")

        if (blockedAppList != null) {
            blockedApps = blockedAppList.toSet()
        }

        blockDuration = intent?.getLongExtra("BLOCK_DURATION", 0L) ?: 0L
        if (blockDuration > 0) {
            countdownTimer = object : CountDownTimer(blockDuration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                    countdownText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    stopSelf()
                }
            }.start()
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocker Service")
            .setContentText("Blocking distracting apps...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val blockedAppList = intent?.getStringArrayListExtra("BLOCK_APPS")
//
//        Log.e("blockedAppList 1", "blockedAppList: $blockedAppList")
//        if (blockedAppList != null) {
//            blockedApps = blockedAppList.toSet()
//        }
//
//        blockDuration = intent?.getLongExtra("BLOCK_DURATION", 0L) ?: 0L
//        if (blockDuration > 0) {
//            countdownTimer = object : CountDownTimer(blockDuration, 1000) {
//                @TargetApi(Build.VERSION_CODES.GINGERBREAD)
//                override fun onTick(millisUntilFinished: Long) {
//                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
//                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
//                    countdownText.text = String.format("%02d:%02d", minutes, seconds)
//                }
//
//                override fun onFinish() {
//                    stopSelf()
//                }
//            }.start()
//            stopHandler = Handler()
//            blockStartTime = System.currentTimeMillis()
//            stopHandler?.postDelayed({
//                stopSelf()
//            }, blockDuration)
//        }
//
//        // Create a notification
//        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("App Blocker Service")
//            .setContentText("Blocking distracting apps...")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .build()
//
//        // Start the service in the foreground with the notification
//
//
//        startForeground(NOTIFICATION_ID, notification)
//
//        return START_STICKY
//    }

    private val checkForegroundApp = object : Runnable {
        override fun run() {
            val foregroundApp =
                getForegroundApp(this@AppBlockerService) ?: getBackgroundApp(this@AppBlockerService)

            Log.e("blockedApps", "blockedApps app: $blockedApps")


            if (foregroundApp != null && blockedApps.contains(foregroundApp)) {
                overlayLayout.visibility = LinearLayout.VISIBLE
            } else {
                overlayLayout.visibility = LinearLayout.GONE
            }
            handler.postDelayed(this, 1000) // Check every second
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkForegroundApp)
        windowManager.removeView(overlayLayout)
        stopHandler?.removeCallbacksAndMessages(null)
        countdownTimer?.cancel()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getForegroundApp(context: Context): String? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 // Check usage in the last minute
        val usageStatsList =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)

        var recentUsageStats: UsageStats? = null
        for (usageStats in usageStatsList) {
            if (recentUsageStats == null || usageStats.lastTimeUsed > recentUsageStats.lastTimeUsed) {
                recentUsageStats = usageStats
            }
        }
        return recentUsageStats?.packageName
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getBackgroundApp(context: Context): String? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 // Check usage in the last minute
        val usageStatsList =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)

        var recentUsageStats: UsageStats? = null
        for (usageStats in usageStatsList) {
            if (recentUsageStats == null || usageStats.lastTimeUsed > recentUsageStats.lastTimeUsed) {
                recentUsageStats = usageStats
            }
        }
        return recentUsageStats?.packageName
    }
}