package com.example.core.logging

import com.example.core.model.LogCategory
import com.example.core.model.LogEvent
import com.example.core.model.LogSeverity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Collections
import java.util.LinkedList

object EventLogger {

    private const val MAX_EVENTS = 400
    private val eventList = Collections.synchronizedList(LinkedList<LogEvent>())
    private val _eventsFlow = MutableStateFlow<List<LogEvent>>(emptyList())
    val eventsFlow: StateFlow<List<LogEvent>> = _eventsFlow.asStateFlow()

    init {
        log(
            severity = LogSeverity.INFO,
            category = LogCategory.SYSTEM,
            message = "Pixel Booster diagnostic engine initialized."
        )
    }

    fun log(
        severity: LogSeverity,
        category: LogCategory,
        message: String,
        errorDetails: String? = null
    ) {
        val event = LogEvent(
            severity = severity,
            category = category,
            message = message,
            errorDetails = errorDetails
        )
        synchronized(eventList) {
            if (eventList.size >= MAX_EVENTS) {
                eventList.removeFirst()
            }
            eventList.add(event)
            _eventsFlow.value = eventList.toList().reversed()
        }
    }

    fun clear() {
        synchronized(eventList) {
            eventList.clear()
            _eventsFlow.value = emptyList()
        }
        log(LogSeverity.INFO, LogCategory.SETTINGS, "Local diagnostic event log cleared by user.")
    }

    fun generateFormattedExport(additionalTelemetry: String? = null): String {
        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("PIXEL BOOSTER ADVANCED — DIAGNOSTIC LOG\n")
        sb.append("Generated at: ${java.util.Date()}\n")
        sb.append("========================================\n\n")

        if (!additionalTelemetry.isNullOrBlank()) {
            sb.append("--- CURRENT SYSTEM TELEMETRY ---\n")
            sb.append(additionalTelemetry)
            sb.append("\n\n")
        }

        sb.append("--- IN-APP EVENT LOGS (${eventList.size} entries) ---\n")
        val currentEvents = synchronized(eventList) { eventList.toList() }
        if (currentEvents.isEmpty()) {
            sb.append("No recorded log events.\n")
        } else {
            currentEvents.forEach { event ->
                sb.append("[${event.formattedDate}] [${event.severity.name}] [${event.category.name}] ${event.message}\n")
                event.errorDetails?.let { sb.append("  Details: $it\n") }
            }
        }
        sb.append("\n========================================\n")
        sb.append("End of diagnostic log.\n")
        return sb.toString()
    }
}
