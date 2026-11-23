package com.raqeeb.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raqeeb.app.widget.DailyAzkarReceiver

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            RaqeebKeepAliveService.start(context)
            DailyAzkarReceiver.scheduleDailyAlarms(context)
        }
    }
}
