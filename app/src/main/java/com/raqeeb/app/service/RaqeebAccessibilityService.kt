package com.raqeeb.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.raqeeb.app.data.SupabaseSyncClient
import com.raqeeb.app.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RaqeebAccessibilityService : AccessibilityService() {

    companion object {
        val restrictedPackages = mutableSetOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "com.facebook.katana",
            "com.facebook.lite",
            "com.facebook.orca",
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "org.telegram.messenger",
            "com.snapchat.android",
            "com.twitter.android",
            "com.google.android.youtube"
        )
        var isProtectionActive: Boolean = true
        var isStrictMode: Boolean = false
        private var alertCounter = 0
    }

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pendingRunnable: Runnable? = null
    private var currentForegroundPkg: String? = null

    private val calmingReminders = listOf(
        "﴿ أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ ﴾ — سورة العلق",
        "﴿ قُل لِّلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ ﴾ — سورة النور",
        "«مَن تَرَكَ شيئًا للهِ عوَّضَه اللهُ خيرًا منه» — حديث صحيح",
        "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ — سورة الرعد",
        "استعن بالله واحفظ بصرك ووقتك فيما ينفعك 🌿"
    )

    private val adminPollRunnable = object : Runnable {
        override fun run() {
            checkAdminMessagesInBackground()
            handler.postDelayed(this, 15_000L)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.post(adminPollRunnable)
    }

    private fun checkAdminMessagesInBackground() {
        serviceScope.launch {
            val msg = SupabaseSyncClient.fetchLatestAdminMessage() ?: return@launch
            if (msg.body.isBlank()) return@launch
            val prefs = getSharedPreferences("raqeeb_prefs", Context.MODE_PRIVATE)
            val lastSeenId = prefs.getLong("last_admin_msg_id", -1L)
            if (msg.id > lastSeenId) {
                prefs.edit().putLong("last_admin_msg_id", msg.id).apply()
                handler.post {
                    PermissionHelper.sendReminderNotification(this@RaqeebAccessibilityService, msg.title, msg.body)
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isProtectionActive || event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui") return

        currentForegroundPkg = pkg
        pendingRunnable?.let { handler.removeCallbacks(it) }

        if (restrictedPackages.contains(pkg)) {
            if (isStrictMode) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                triggerAlternatingAlert(calmingReminders.random())
                return
            }

            val runnable = Runnable {
                if (currentForegroundPkg == pkg && isProtectionActive) {
                    triggerAlternatingAlert(calmingReminders.random())
                }
            }
            pendingRunnable = runnable
            handler.postDelayed(runnable, 5_000L)
        }
    }

    private fun triggerAlternatingAlert(text: String) {
        alertCounter++
        if (alertCounter % 2 == 1) {
            // المرة الفردية: رسالة Toast أنيقة من أسفل الشاشة
            Toast.makeText(applicationContext, "🌿 رقيب: $text", Toast.LENGTH_LONG).show()
        } else {
            // المرة الزوجية: إشعار في شريط الإشعارات
            PermissionHelper.sendReminderNotification(
                this,
                "تذكير لطيف من رقيب 🌿",
                text
            )
        }
    }

    override fun onInterrupt() {
        pendingRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacks(adminPollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(adminPollRunnable)
        super.onDestroy()
    }
}
