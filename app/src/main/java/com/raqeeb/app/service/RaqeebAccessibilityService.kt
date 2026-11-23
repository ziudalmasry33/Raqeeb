package com.raqeeb.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.raqeeb.app.util.PermissionHelper

class RaqeebAccessibilityService : AccessibilityService() {

    companion object {
        val restrictedPackages = mutableSetOf<String>()
        var isProtectionActive: Boolean = true
        var isStrictMode: Boolean = false
    }

    private val handler = Handler(Looper.getMainLooper())
    private var pendingRunnable: Runnable? = null
    private var currentForegroundPkg: String? = null

    private val calmingReminders = listOf(
        "﴿ أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ ﴾ — سورة العلق (14)",
        "﴿ وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ﴾ — سورة الطلاق (2)",
        "«مَن تَرَكَ شيئًا للهِ عوَّضَه اللهُ خيرًا منه» — حديث صحيح (رواه أحمد)",
        "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ — سورة الرعد (28)",
        "«اللهم إني أعوذ بك من شر سمعي، ومن شر بصري، ومن شر قلبي» — صحيح أبي داود"
    )

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
                PermissionHelper.sendReminderNotification(
                    this,
                    "رقيب يحميك 🌿",
                    calmingReminders.random()
                )
                return
            }

            // جدولة الإشعار بعد 5 ثوانٍ من فتح التطبيق المقيد
            val runnable = Runnable {
                if (currentForegroundPkg == pkg && isProtectionActive) {
                    PermissionHelper.sendReminderNotification(
                        this,
                        "تذكير لطيف من رقيب 🌿",
                        calmingReminders.random()
                    )
                }
            }
            pendingRunnable = runnable
            handler.postDelayed(runnable, 5_000L)
        }
    }

    override fun onInterrupt() {
        pendingRunnable?.let { handler.removeCallbacks(it) }
    }
}
