package com.raqeeb.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.raqeeb.app.util.PermissionHelper

class RaqeebAccessibilityService : AccessibilityService() {

    companion object {
        val restrictedPackages = mutableSetOf<String>()
        var isProtectionActive: Boolean = true
        var isStrictMode: Boolean = false
        private var lastAlertTime = 0L
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isProtectionActive || event == null) return
        val pkg = event.packageName?.toString() ?: return

        if (restrictedPackages.contains(pkg)) {
            val now = System.currentTimeMillis()
            if (now - lastAlertTime > 15_000L) {
                lastAlertTime = now
                PermissionHelper.sendReminderNotification(
                    this,
                    "رقيب يذكّرك 🌿",
                    "﴿ أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ ﴾ — تذكّر هدفك واستعن بالله."
                )
            }
            if (isStrictMode) {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    override fun onInterrupt() {}
}
