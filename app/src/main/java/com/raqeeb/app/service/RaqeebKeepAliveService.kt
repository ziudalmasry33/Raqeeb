package com.raqeeb.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.raqeeb.app.MainActivity
import com.raqeeb.app.R
import com.raqeeb.app.data.SupabaseSyncClient
import com.raqeeb.app.data.TrustedAzkarLibrary
import com.raqeeb.app.util.PermissionHelper
import com.raqeeb.app.widget.DailyAzkarReceiver
import kotlinx.coroutines.*

class RaqeebKeepAliveService : Service() {

    companion object {
        const val CHANNEL_ID = "raqeeb_background_guard_channel"
        const val NOTIFICATION_ID = 2026
        var isServiceRunning = false
        private var alertCounter = 0

        fun start(context: Context) {
            val intent = Intent(context, RaqeebKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastTrackedPackage: String? = null
    private var packageEnteredAtMs: Long = 0L
    private var remindedForCurrentSession: Boolean = false

    private val calmingMessages = listOf(
        "اعلم أن الله يراك — ﴿ أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ ﴾",
        "اعلم أن الله يراك — ﴿ قُل لِّلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ ﴾",
        "اعلم أن الله يراك — «مَن تَرَكَ شيئًا للهِ عوَّضَه اللهُ خيرًا منه»",
        "اعلم أن الله يراك — ﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾"
    )

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildPersistentNotification())
        DailyAzkarReceiver.scheduleDailyAlarms(applicationContext)
        startBackgroundSyncLoop()
        startAppUsage5SecWatcher()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun getCurrentForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 10_000L, now)
        val event = UsageEvents.Event()
        var topPkg: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                topPkg = event.packageName
            }
        }
        return topPkg
    }

    private fun startAppUsage5SecWatcher() {
        serviceScope.launch {
            while (isActive) {
                try {
                    if (RaqeebAccessibilityService.isProtectionActive) {
                        val fgPkg = getCurrentForegroundPackage()
                        val now = System.currentTimeMillis()
                        if (!fgPkg.isNullOrBlank() &&
                            fgPkg != applicationContext.packageName &&
                            fgPkg != "com.android.systemui"
                        ) {
                            if (fgPkg != lastTrackedPackage) {
                                lastTrackedPackage = fgPkg
                                packageEnteredAtMs = now
                                remindedForCurrentSession = false
                            } else if (!remindedForCurrentSession && (now - packageEnteredAtMs) >= 5_000L) {
                                remindedForCurrentSession = true
                                val chosen = calmingMessages.random()
                                withContext(Dispatchers.Main) {
                                    triggerAlternatingReminder(chosen)
                                }
                            }
                        } else {
                            lastTrackedPackage = null
                            remindedForCurrentSession = false
                        }
                    }
                } catch (_: Exception) {
                }
                delay(1_000L)
            }
        }
    }

    private fun triggerAlternatingReminder(message: String) {
        alertCounter++
        if (alertCounter % 2 == 1) {
            Toast.makeText(applicationContext, "🌿 $message", Toast.LENGTH_LONG).show()
        } else {
            PermissionHelper.sendReminderNotification(
                applicationContext,
                "تذكير رقيب 🌿",
                message
            )
        }
    }

    private fun startBackgroundSyncLoop() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val msg = SupabaseSyncClient.fetchLatestAdminMessage()
                    if (msg != null && msg.body.isNotBlank()) {
                        val prefs = getSharedPreferences("raqeeb_prefs", Context.MODE_PRIVATE)
                        val lastSeenId = prefs.getLong("last_admin_msg_id", -1L)
                        if (msg.id > lastSeenId) {
                            prefs.edit().putLong("last_admin_msg_id", msg.id).apply()
                            withContext(Dispatchers.Main) {
                                PermissionHelper.sendReminderNotification(
                                    applicationContext,
                                    msg.title,
                                    msg.body
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                }
                delay(12_000L)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "حماية رقيب المستمرة في الخلفية",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "يُبقي درع رقيب والتذكيرات الإيمانية نشطة في الخلفية"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🛡️ رقيب يعمل في الخلفية")
            .setContentText("تذكير تلقائي بعد 5 ثوانٍ وحماية مستمرة")
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        start(applicationContext)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        isServiceRunning = false
        serviceScope.cancel()
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
