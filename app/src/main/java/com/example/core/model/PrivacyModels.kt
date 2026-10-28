package com.example.core.model

import java.util.Locale

data class PrivacySensorState(
    val sensorName: String,
    val sensorType: PrivacySensorType,
    val isEnabled: Boolean,
    val isCurrentlyInUse: Boolean,
    val lastAccessedApp: String,
    val description: String
)

enum class PrivacySensorType {
    CAMERA,
    MICROPHONE,
    LOCATION,
    USB_OTG,
    BLUETOOTH,
    NFC
}

data class PrivacyReport(
    val activeSensorsCount: Int,
    val blockedTrackersCount: Int,
    val zeroEgressStatus: String = "100% On-Device Protection",
    val permissionAuditScore: Int = 96,
    val sensors: List<PrivacySensorState>
)
