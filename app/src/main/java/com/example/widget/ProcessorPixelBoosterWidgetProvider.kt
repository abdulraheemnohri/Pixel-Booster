package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.platform.DeviceDiagnosticsProvider

class ProcessorPixelBoosterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_pixel_booster_processor)
                val diagnostics = DeviceDiagnosticsProvider(context)
                // Assuming we can get CPU info; using a placeholder if needed
                val telemetry = diagnostics.getThermalStatus() // Placeholder for actual CPU telemetry
                val cpuUsedPercent = 45 // Placeholder logic

                views.setTextViewText(R.id.widget_cpu_text, "CPU: $cpuUsedPercent% | GPU: 20%")
                views.setProgressBar(R.id.widget_cpu_progress, 100, cpuUsedPercent, false)

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                android.util.Log.e("ProcessorWidget", "Failed to update widget", e)
            }
        }
    }
}
