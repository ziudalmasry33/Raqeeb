package com.raqeeb.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raqeeb.app.util.PermissionHelper
import java.util.Calendar

class DailyAzkarReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("azkar_type") ?: "morning"
        when (type) {
            "morning" -> PermissionHelper.sendReminderNotification(
                context,
                "☀️ أذكار الصباح | رقيب",
                "أصبحنا وأصبح الملك لله.. ابدأ يومك بحفظ بصرك وذكر الله."
            )
            "evening" -> PermissionHelper.sendReminderNotification(
                context,
                "🌙 أذكار المساء | رقيب",
                "أمسينا وأمسى الملك لله.. حصّن نفسك بأذكار المساء."
            )
            "wird" -> PermissionHelper.sendReminderNotification(
                context,
                "📖 الورد اليومي | رقيب",
                "لا تنسَ وردك اليومي من القرآن الكريم وتسجيل إنجازك في التحديات."
            )
        }
    }

    companion object {
        fun scheduleDailyAlarms(context: Context) {
            scheduleAt(context, 101, 7, 30, "morning")
            scheduleAt(context, 102, 17, 30, "evening")
            scheduleAt(context, 103, 21, 0, "wird")
        }

        private fun scheduleAt(context: Context, reqCode: Int, hour: Int, minute: Int, type: String) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyAzkarReceiver::class.java).apply {
                putExtra("azkar_type", type)
            }
            val pending = PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            alarmMgr.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pending
            )
        }
    }
}
