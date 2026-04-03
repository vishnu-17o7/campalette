package com.vishnu.campalette.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

enum class AtelierHapticEvent {
    Navigation,
    PermissionPrompt,
    Selection,
    Inspect,
    Copy,
    ToggleOn,
    ToggleOff,
    CapturePress,
    CaptureSuccess,
    Sample,
    SampleExplore,
    AddToPalette,
    Save
}

class AtelierHaptics(
    context: Context,
    private val enabled: () -> Boolean
) {
    private val appContext = context.applicationContext

    fun perform(event: AtelierHapticEvent, force: Boolean = false) {
        if (!force && !enabled()) return
        val vibrator = resolveVibrator() ?: return
        if (!vibrator.hasVibrator()) return

        val pattern = when (event) {
            AtelierHapticEvent.Navigation ->
                Waveform(longArrayOf(0, 10, 14, 8), intArrayOf(0, 56, 0, 34))
            AtelierHapticEvent.PermissionPrompt ->
                Waveform(longArrayOf(0, 16, 20, 14), intArrayOf(0, 92, 0, 132))
            AtelierHapticEvent.Selection ->
                Waveform(longArrayOf(0, 12, 16, 10), intArrayOf(0, 64, 0, 88))
            AtelierHapticEvent.Inspect ->
                Waveform(longArrayOf(0, 14, 18, 12), intArrayOf(0, 74, 0, 104))
            AtelierHapticEvent.Copy ->
                Waveform(longArrayOf(0, 8, 18, 14), intArrayOf(0, 54, 0, 132))
            AtelierHapticEvent.ToggleOn ->
                Waveform(longArrayOf(0, 12, 14, 20), intArrayOf(0, 72, 0, 148))
            AtelierHapticEvent.ToggleOff ->
                Waveform(longArrayOf(0, 18, 22, 10), intArrayOf(0, 120, 0, 52))
            AtelierHapticEvent.CapturePress ->
                Waveform(longArrayOf(0, 20, 24, 14), intArrayOf(0, 168, 0, 112))
            AtelierHapticEvent.CaptureSuccess ->
                Waveform(longArrayOf(0, 14, 22, 18, 28, 28), intArrayOf(0, 96, 0, 142, 0, 208))
            AtelierHapticEvent.Sample ->
                Waveform(longArrayOf(0, 10, 12, 14), intArrayOf(0, 48, 0, 90))
            AtelierHapticEvent.SampleExplore ->
                Waveform(longArrayOf(0, 18, 24, 14, 22, 12), intArrayOf(0, 122, 0, 84, 0, 62))
            AtelierHapticEvent.AddToPalette ->
                Waveform(longArrayOf(0, 12, 18, 16), intArrayOf(0, 80, 0, 150))
            AtelierHapticEvent.Save ->
                Waveform(longArrayOf(0, 10, 14, 12, 16, 20), intArrayOf(0, 68, 0, 108, 0, 188))
        }

        vibrate(vibrator, pattern)
    }

    private fun resolveVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun vibrate(vibrator: Vibrator, pattern: Waveform) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern.timings, pattern.amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern.timings, -1)
        }
    }

    private data class Waveform(
        val timings: LongArray,
        val amplitudes: IntArray
    )
}

@Composable
fun rememberAtelierHaptics(enabled: Boolean): AtelierHaptics {
    val context = LocalContext.current
    val latestEnabled = rememberUpdatedState(enabled)
    return remember(context) {
        AtelierHaptics(context) { latestEnabled.value }
    }
}
