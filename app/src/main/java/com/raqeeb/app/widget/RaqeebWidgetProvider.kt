package com.raqeeb.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.raqeeb.app.MainActivity
import com.raqeeb.app.R
import com.raqeeb.app.data.TrustedAzkarLibrary

class RaqeebWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidgetView(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_NEXT_WIDGET_ZIKR") {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, RaqeebWidgetProvider::class.java))
            for (id in ids) updateWidgetView(context, mgr, id)
        }
    }

    companion object {
        fun updateWidgetView(context: Context, mgr: AppWidgetManager, widgetId: Int) {
            val item = TrustedAzkarLibrary.allReminders.random()
            val views = RemoteViews(context.packageName, R.layout.widget_raqeeb).apply {
                setTextViewText(R.id.widgetZikrText, item.textAr)
                setTextViewText(R.id.widgetZikrSource, item.sourceAr)

                val openIntent = PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widgetRoot, openIntent)

                val refreshIntent = PendingIntent.getBroadcast(
                    context, 1,
                    Intent(context, RaqeebWidgetProvider::class.java).apply { action = "ACTION_NEXT_WIDGET_ZIKR" },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widgetRefreshBtn, refreshIntent)
            }
            mgr.updateAppWidget(widgetId, views)
        }
    }
}
