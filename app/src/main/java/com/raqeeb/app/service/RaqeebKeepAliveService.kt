package com.raqeeb.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.raqeeb.app.MainActivity
import com.raqeeb.app.R
import com.raqeeb.app.data.SupabaseSyncClient
import com.raqeeb.app.util.PermissionHelper
import com.raqeeb.app.widget.DailyAzkarReceiver
import kotlinx.coroutines.*

class RaqeebKeepAliveService : Service() {

    companion object {
        const val CHANNEL_ID = "raqeeb_background_guard_channel"
        const val NOTIFICATION_ID = 2026
        var isServiceRunning = false

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

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildPersistentNotification())
        DailyAzkarReceiver.scheduleDailyAlarms(applicationContext)
        startBackgroundSyncLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY يضمن أن نظام أندرويد يعيد تشغيل الخدمة تلقائياً إذا تم إغلاق التطبيق
        return START_STICKY
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
                delay(15_000L)
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
            .setContentText("الحماية الإيمانية والتذكيرات نشطة لحفظ بصرك ووقتك")
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // إعادة جدولة الخدمة فوراً إذا مسح المستخدم التطبيق من قائمة التطبيقات المفتوحة
        val restartIntent = Intent(applicationContext, RaqeebKeepAliveService::class.java)
        start(applicationContext)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        isServiceRunning = false
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
