package com.example.feature.diagnostics

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DiagnosticsUiState(
    val isRunningScan: Boolean = false,
    val touchPointsTested: Int = 0,
    val vibrationTested: Boolean = false,
    val audioTested: Boolean = false,
    val compassHeading: Float = 0f,
    val accelerometerG: Float = 0f,
    val playIntegrityPassed: Boolean = false,
    val biometricsAvailable: Boolean = false,
    val diagnosticLog: List<String> = listOf(
        "Diagnostics ready. Run an individual test to record a result.",
        "Sensor calibration, Play Integrity, and secure hardware checks have not been run by this screen."
    )
)

class DiagnosticsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    private val app = application

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerHapticTest() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}

        val available = vibrator != null
        _uiState.update { state ->
            state.copy(
                vibrationTested = available,
                diagnosticLog = listOf(
                    if (available) "Haptic vibration command sent; physical actuator response was not independently verified."
                    else "Haptic test unavailable: this device exposes no vibrator service."
                ) + state.diagnosticLog
            )
        }
    }

    fun recordTouchPoint() {
        _uiState.update { state ->
            val newCount = state.touchPointsTested + 1
            state.copy(
                touchPointsTested = newCount,
                diagnosticLog = if (newCount == 1) listOf("Touch-test area tapped; this records interaction only and does not verify all digitizer points.") + state.diagnosticLog else state.diagnosticLog
            )
        }
    }

    fun testAudioSpeaker() {
        // Do not report success until a real audible test tone is played and confirmed.
        _uiState.update { state ->
            state.copy(
                audioTested = false,
                diagnosticLog = listOf(
                    "Speaker test is not implemented yet; no audio tone was played and no result was recorded."
                ) + state.diagnosticLog
            )
        }
    }

    fun runFullHardwareDiagnostic() {
        _uiState.update { it.copy(isRunningScan = true) }
        Handler(Looper.getMainLooper()).postDelayed({
            _uiState.update { state ->
                state.copy(
                    isRunningScan = false,
                    diagnosticLog = listOf(
                        "Diagnostic workflow completed. This screen does not perform a full hardware integrity scan.",
                        "No conclusion was made about CPU throttling, storage integrity, secure hardware, or defect counts."
                    ) + state.diagnosticLog
                )
            }
        }, 1200)
    }
}
