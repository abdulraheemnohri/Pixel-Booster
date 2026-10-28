package com.example

import com.example.core.logging.EventLogger
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import org.junit.Assert.assertTrue
import org.junit.Test

class EventLoggerTest {

    @Test
    fun `event logger adds events and generates formatted export`() {
        EventLogger.clear()

        EventLogger.log(
            severity = LogSeverity.INFO,
            category = LogCategory.TELEMETRY,
            message = "Testing telemetry logging"
        )
        EventLogger.log(
            severity = LogSeverity.WARNING,
            category = LogCategory.THERMAL,
            message = "Testing thermal warning"
        )

        val events = EventLogger.eventsFlow.value
        assertTrue("Expected at least 2 events", events.size >= 2)

        val export = EventLogger.generateFormattedExport("Test Device: Pixel 6")
        assertTrue("Export must contain header", export.contains("PIXEL BOOSTER ADVANCED"))
        assertTrue("Export must contain telemetry", export.contains("Test Device: Pixel 6"))
        assertTrue("Export must contain log entries", export.contains("Testing thermal warning"))
    }
}
