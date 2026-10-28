package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.core.optimizer.SystemOptimizer
import com.example.core.platform.DeviceDiagnosticsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PixelBoosterWidgetProvider : AppWidgetProvider() {

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
            appWidgetId: Int,
            overrideStatus: String? = null
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_pixel_booster)
                val diagnostics = DeviceDiagnosticsProvider(context)

                val mem = diagnostics.getMemoryInfo()
                val battery = diagnostics.getBatteryInfo()
                val thermal = diagnostics.getThermalStatus()

                val usedPercent = mem.usedPercentage.toInt().coerceIn(0, 100)
                val tempCelsius = battery.temperatureCelsius

                views.setTextViewText(
                    R.id.widget_ram_text,
                    "$usedPercent% Used • ${mem.formattedAvailable} Free"
                )
                views.setProgressBar(R.id.widget_ram_progress, 100, usedPercent, false)
                views.setTextViewText(R.id.widget_battery_text, "🔋 ${battery.levelPercentage}%")
                views.setTextViewText(
                    R.id.widget_temp_text,
                    if (tempCelsius != null) "🌡 ${String.format(Locale.US, "%.1f", tempCelsius)}°C" else "🌡 Temp N/A"
                )

                val status = overrideStatus ?: when {
                    mem.usedPercentage > 85f -> "STATUS: RAM HIGH"
                    thermal.statusCode >= 2 -> "STATUS: THERMAL ELEVATED"
                    else -> "STATUS: OPTIMAL"
                }
                views.setTextViewText(R.id.widget_status_text, status)

                // Open app on header or root tap
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

                // 1-Tap Optimize Broadcast Trigger
                // Open the guided Smart Boost flow; Android does not allow apps to guarantee
                // system-wide RAM cleanup from a home-screen button.
                views.setOnClickPendingIntent(R.id.widget_btn_optimize, openAppPendingIntent)
                views.setTextViewText(R.id.widget_btn_optimize, "OPEN SMART BOOST")

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Log and ignore to prevent "Can't load widget"
                android.util.Log.e("PixelBoosterWidget", "Failed to update widget", e)
            }
        }
    }
}
