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
import java.util.Locale

class CompactPixelBoosterWidgetProvider : AppWidgetProvider() {

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
                val views = RemoteViews(context.packageName, R.layout.widget_pixel_booster_compact)
                val diagnostics = DeviceDiagnosticsProvider(context)
                val battery = diagnostics.getBatteryInfo()
                val tempCelsius = battery.temperatureCelsius

                views.setTextViewText(R.id.widget_battery_text, "🔋 ${battery.levelPercentage}%")
                views.setTextViewText(
                    R.id.widget_temp_text,
                    if (tempCelsius != null) "🌡 ${String.format(Locale.US, "%.1f", tempCelsius)}°C" else "🌡 Temp N/A"
                )

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
                android.util.Log.e("CompactWidget", "Failed to update widget", e)
            }
        }
    }
}
