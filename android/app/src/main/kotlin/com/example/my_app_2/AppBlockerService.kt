package com.example.my_app_2

import android.R as androidR
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
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.app.NotificationCompat
import com.example.my_app_2.R
import java.util.concurrent.TimeUnit

class AppBlockerService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayLayout: RelativeLayout

    private val handler = Handler()
    private lateinit var blockedApps: MutableSet<String>
    private var stopHandler: Handler? = null
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "App Blocker Service"

    private var blockStartTime: Long = 0L
    private var blockDuration: Long = 0L

    private lateinit var countdownText: TextView
    private var countdownTimer: CountDownTimer? = null
    private lateinit var progressBar : ProgressBar
    private lateinit var textView : TextView
    private lateinit var progressBar1 : ProgressBar
    private lateinit var progressBar2 : ProgressBar
    private var lastForegroundApp: String? = null
    private var foregroundApp: String? = null


    // Create a map to store CountDownTimer and progress bars for each blocked app
    private val timers: MutableMap<String, CountDownTimer> = mutableMapOf()
    private val progressBars1: MutableMap<String, ProgressBar> = mutableMapOf()
    private val progressBars2: MutableMap<String, ProgressBar> = mutableMapOf()


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

//        blockedApps  = setOf("com.example.my_app_2", "com.google.android.youtube", "com.zhiliaoapp.musically")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Inflate the layout
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayLayout = inflater.inflate(R.layout.overview_layout, null) as RelativeLayout
        textView = overlayLayout.findViewById<TextView>(R.id.textView_timerview_time)
        progressBar1 = overlayLayout.findViewById<ProgressBar>(R.id.progressbar_timerview)
        progressBar2 = overlayLayout.findViewById<ProgressBar>(R.id.progressbar1_timerview)


//        overlayLayout = LinearLayout(this)
//        overlayLayout.orientation = LinearLayout.VERTICAL
//        overlayLayout.gravity = Gravity.CENTER
//        overlayLayout.setBackgroundColor(0x7F000000)
//
//        val overlayImage = ImageView(this)
//        overlayImage.setImageResource(R.mipmap.ic_launcher)
//        overlayLayout.addView(overlayImage)
//
//
//        progressBar = ProgressBar(this, null, androidR.attr.progressBarStyleHorizontal)
//        progressBar.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        progressBar.isIndeterminate = false
//        progressBar.progressDrawable = resources.getDrawable(R.drawable.circular_progress_bar)
//        overlayLayout.addView(progressBar)
//
//        countdownText = TextView(this)
//        countdownText.setTextColor(0xFFFFFFFF.toInt())
//        countdownText.textSize = 24f
//        countdownText.gravity = Gravity.CENTER
//        overlayLayout.addView(countdownText)
//
//        val overlayText = TextView(this)
//        overlayText.text = "App Blocked, Take a Break!"
////        overlayText.setBackgroundColor(0x7F000000)
//        overlayText.setTextColor(0xFFFFFFFF.toInt())
//        overlayText.textSize = 24f
//        overlayText.gravity = Gravity.CENTER
//        overlayLayout.addView(overlayText)
//
//        // Add a Button to the overlayLayout
//        val stopBlockingButton = Button(this)
//        stopBlockingButton.text = "Stop Blocking"
//        stopBlockingButton.setOnClickListener {
//            // Stop the service when the button is clicked
//            stopSelf()
//        }
//        overlayLayout.addView(stopBlockingButton)
//
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
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
        val stopPackage = intent?.getStringExtra("STOP_PACKAGE")

        if (blockedAppList != null) {
            blockedApps = blockedAppList.toMutableSet()
        }

        if (stopPackage != null) {
            blockedApps.remove(stopPackage).apply {
                if (blockedApps.isEmpty()) {
                    stopSelf()
                }
            }
        }

        blockDuration = intent?.getLongExtra("BLOCK_DURATION", 0L) ?: 0L
        if (blockDuration > 0) {

            for (packageName in blockedApps) {
                val progressBar1 = ProgressBar(this, null, androidR.attr.progressBarStyleHorizontal)
                val progressBar2 = ProgressBar(this, null, androidR.attr.progressBarStyleHorizontal)
                progressBars1[packageName] = progressBar1
                progressBars2[packageName] = progressBar2

                val timer = object : CountDownTimer(blockDuration, 1000) {
                    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
                    override fun onTick(millisUntilFinished: Long) {
                        val progress = ((blockDuration - millisUntilFinished) / (blockDuration / 100)).toInt()
                        progressBar1.progress = progress
                        progressBar2.progress = progress

                        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                        textView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                        handler.post(updateProgressBars)
                        Log.d("AppBlockerService Main", "onTick called, progress: $progress")
                    }

                    override fun onFinish() {
                        stopSelf()
                    }
                }
                timers[packageName] = timer
                timer.start()
            }

//
//            stopHandler = Handler()
//            blockStartTime = System.currentTimeMillis()
//            stopHandler?.postDelayed({
//                stopSelf()
//            }, blockDuration)
//
//            countdownTimer = object : CountDownTimer(blockDuration, 1000) {
//                override fun onTick(millisUntilFinished: Long) {
//                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
//                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
//                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
////                    countdownText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
//                    textView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
//                    progressBar1.progress = ((blockDuration - millisUntilFinished) / (blockDuration / 100)).toInt()
//                    progressBar2.progress = ((blockDuration - millisUntilFinished) / (blockDuration / 100)).toInt()
//                    // Update the progress bar
//                    val progress = ((blockDuration - millisUntilFinished) / (blockDuration / 100)).toInt()
////                    progressBar.progress = progress
//                }
//
//                override fun onFinish() {
//                    stopSelf()
//                }
//            }.start()
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocker Service")
            .setContentText("Blocking distracting apps...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }


//    private val checkForegroundApp = object : Runnable {
//        override fun run() {
//            val foregroundApp =
//                getForegroundApp(this@AppBlockerService) ?: getBackgroundApp(this@AppBlockerService)
//
//            if (foregroundApp != lastForegroundApp) {
//                if (foregroundApp != null && blockedApps.contains(foregroundApp)) {
//                    overlayLayout.visibility = RelativeLayout.VISIBLE
//                } else {
//                    overlayLayout.visibility = RelativeLayout.GONE
//                }
//                lastForegroundApp = foregroundApp
//            }
//
//            handler.postDelayed(this, 1000) // Check every second
//        }
//    }


    private val checkForegroundApp = object : Runnable {
        override fun run() {
            foregroundApp =
                getForegroundApp(this@AppBlockerService) ?: getBackgroundApp(this@AppBlockerService)

            if (foregroundApp != lastForegroundApp) {
                if (foregroundApp != null && blockedApps.contains(foregroundApp)) {
                    overlayLayout.visibility = RelativeLayout.VISIBLE

                    // Update the progress bars based on the foreground app
//                    val progressBar1 = progressBars1[foregroundApp]
//                    val progressBar2 = progressBars2[foregroundApp]
//                    overlayLayout.findViewById<ProgressBar>(R.id.progressbar_timerview).progress = progressBar1?.progress ?: 0
//                    overlayLayout.findViewById<ProgressBar>(R.id.progressbar1_timerview).progress = progressBar2?.progress ?: 0

                    Log.d("AppBlockerService Main2", "Progress bars updated, progress1: ${progressBar1.progress}, progress2: ${progressBar2?.progress}")
                } else {
                    overlayLayout.visibility = RelativeLayout.GONE
                }
                lastForegroundApp = foregroundApp
            }


//            if (foregroundApp != lastForegroundApp) {
//                if (foregroundApp != null && blockedApps.contains(foregroundApp)) {
//                    overlayLayout.visibility = RelativeLayout.VISIBLE
//
//                    // Update the progress bars based on the foreground app
//                    val progressBar1 = progressBars1[foregroundApp]
//                    val progressBar2 = progressBars2[foregroundApp]
//                    overlayLayout.findViewById<ProgressBar>(R.id.progressbar_timerview).progress = progressBar1?.progress ?: 0
//                    overlayLayout.findViewById<ProgressBar>(R.id.progressbar1_timerview).progress = progressBar2?.progress ?: 0
//
//                    Log.d("AppBlockerService Main2", "Progress bars updated, progress1: ${progressBar1?.progress}, progress2: ${progressBar2?.progress}")
//
//                } else {
//                    overlayLayout.visibility = RelativeLayout.GONE
//                }
//                lastForegroundApp = foregroundApp
//            }

            handler.postDelayed(this, 1000) // Check every second
        }
    }

    private val updateProgressBars = object : Runnable {
        override fun run() {
            val progressBar1 = progressBars1[foregroundApp]
            val progressBar2 = progressBars2[foregroundApp]
            overlayLayout.findViewById<ProgressBar>(R.id.progressbar_timerview).progress = progressBar1?.progress ?: 0
            overlayLayout.findViewById<ProgressBar>(R.id.progressbar1_timerview).progress = progressBar2?.progress ?: 0

            handler.postDelayed(this, 1000) // Update every second
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