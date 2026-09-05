package com.vishnu.campalette.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.VibrationAttributes
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

enum class AtelierHapticEvent {
    Navigation,
    PermissionPrompt,
    PermissionGranted,
    PermissionDenied,
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
    Undo,
    Save,
    Delete,
    Duplicate,
    ReorderStart,
    Reorder,
    ReorderDrop,
    Share,
    Export,
    SheetPresent,
    SheetDismiss,
    Swipe,
    LongPressMenu,
    GalleryImport,
    ColorBlindnessPreview,
    ResetTheme,
    OnboardingNext,
    OnboardingFinish,
    Search,
    Filter,
    Favorite,
    Error
}

/**
 * Event-level tactile language for Campalette.
 *
 * Android 11+ devices receive hardware-tuned primitive compositions when every primitive in a
 * phrase is supported. Older or less capable devices receive a short predefined effect, with a
 * final one-shot fallback only for Android 8 and below. High-frequency events are rate limited so
 * sampling and reordering feel like individual detents instead of a continuous buzz.
 */
class AtelierHaptics(
    context: Context,
    private val enabled: () -> Boolean
) {
    private val appContext = context.applicationContext
    private val vibrator: Vibrator? by lazy(::resolveVibrator)
    private val rateLimiter = HapticRateLimiter(SystemClock::elapsedRealtime)
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    fun perform(event: AtelierHapticEvent, force: Boolean = false) {
        if (!force && !enabled()) return

        // Reject repeated pointer-driven events before touching system services. Reorder and
        // live-sampling callbacks can arrive much faster than a useful tactile detent.
        val phrase = phraseFor(event)
        if (!rateLimiter.tryAcquire(event, phrase.cooldownMs)) return

        if (!systemHapticsEnabled()) return
        val resolvedVibrator = vibrator ?: return
        if (!resolvedVibrator.hasVibrator()) return

        runCatching {
            val effect = compositionEffectOrNull(resolvedVibrator, phrase)
                ?: fallbackEffect(phrase)
            play(resolvedVibrator, effect, phrase.legacyDurationMs)
        }
    }

    private fun resolveVibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Android still documents this compatibility key for custom effects that must honor the
    // device-wide Touch feedback switch, even though the field itself is deprecated on API 33.
    @Suppress("DEPRECATION")
    private fun systemHapticsEnabled(): Boolean = runCatching {
        Settings.System.getInt(
            appContext.contentResolver,
            Settings.System.HAPTIC_FEEDBACK_ENABLED,
            1
        ) != 0
    }.getOrDefault(true)

    @SuppressLint("WrongConstant") // The array is built only from Composition.PRIMITIVE_* constants below.
    private fun compositionEffectOrNull(
        vibrator: Vibrator,
        phrase: HapticPhrase
    ): VibrationEffect? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || phrase.primitives.isEmpty()) return null
        val primitiveIds = phrase.primitives.map(PrimitiveStep::primitiveId).distinct().toIntArray()
        if (!vibrator.areAllPrimitivesSupported(*primitiveIds)) return null

        return phrase.primitives
            .fold(VibrationEffect.startComposition()) { composition, step ->
                composition.addPrimitive(step.primitiveId, step.scale, step.delayMs)
            }
            .compose()
    }

    private fun fallbackEffect(phrase: HapticPhrase): VibrationEffect? {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                VibrationEffect.createPredefined(phrase.fallbackEffect)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                VibrationEffect.createOneShot(
                    phrase.legacyDurationMs,
                    phrase.legacyAmplitude.coerceIn(1, 255)
                )
            else -> null
        }
    }

    private fun play(vibrator: Vibrator, effect: VibrationEffect?, legacyDurationMs: Long) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && effect != null ->
                vibrator.vibrate(
                    effect,
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)
                )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && effect != null -> {
                @Suppress("DEPRECATION")
                vibrator.vibrate(effect, audioAttributes)
            }
            else -> {
                @Suppress("DEPRECATION")
                vibrator.vibrate(legacyDurationMs)
            }
        }
    }

    @SuppressLint("InlinedApi") // Constants are value-inlined and their API calls stay version-gated.
    private fun phraseFor(event: AtelierHapticEvent): HapticPhrase = when (event) {
        AtelierHapticEvent.Navigation -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.50f),
            fallback = VibrationEffect.EFFECT_TICK,
            amplitude = 48,
            cooldown = 55
        )
        AtelierHapticEvent.PermissionPrompt,
        AtelierHapticEvent.SheetPresent -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.45f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.70f, delayMs = 45),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 96
        )
        AtelierHapticEvent.PermissionGranted,
        AtelierHapticEvent.OnboardingFinish -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.55f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.78f, delayMs = 50),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.00f, delayMs = 55),
            fallback = VibrationEffect.EFFECT_DOUBLE_CLICK,
            amplitude = 172,
            duration = 22
        )
        AtelierHapticEvent.PermissionDenied,
        AtelierHapticEvent.Error -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.62f),
            primitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.88f, delayMs = 65),
            fallback = VibrationEffect.EFFECT_HEAVY_CLICK,
            amplitude = 176,
            duration = 22
        )
        AtelierHapticEvent.Selection,
        AtelierHapticEvent.Filter,
        AtelierHapticEvent.ColorBlindnessPreview,
        AtelierHapticEvent.OnboardingNext -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.46f),
            fallback = VibrationEffect.EFFECT_TICK,
            amplitude = 52,
            cooldown = 45
        )
        AtelierHapticEvent.Inspect -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.45f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.70f, delayMs = 45),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 88
        )
        AtelierHapticEvent.Copy -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.42f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.78f, delayMs = 55),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 104
        )
        AtelierHapticEvent.ToggleOn -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.45f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.72f, delayMs = 40),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 92
        )
        AtelierHapticEvent.ToggleOff,
        AtelierHapticEvent.ResetTheme,
        AtelierHapticEvent.SheetDismiss -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.62f),
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.42f, delayMs = 35),
            fallback = VibrationEffect.EFFECT_TICK,
            amplitude = 72
        )
        AtelierHapticEvent.CapturePress -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.82f),
            fallback = VibrationEffect.EFFECT_HEAVY_CLICK,
            amplitude = 176,
            duration = 20,
            cooldown = 120
        )
        AtelierHapticEvent.CaptureSuccess -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.62f),
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.70f, delayMs = 55),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.00f, delayMs = 55),
            fallback = VibrationEffect.EFFECT_DOUBLE_CLICK,
            amplitude = 196,
            duration = 24,
            cooldown = 180
        )
        AtelierHapticEvent.Sample -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.38f),
            fallback = VibrationEffect.EFFECT_TICK,
            amplitude = 38,
            cooldown = 55
        )
        AtelierHapticEvent.SampleExplore,
        AtelierHapticEvent.LongPressMenu,
        AtelierHapticEvent.ReorderStart -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.38f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.68f, delayMs = 45),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 98,
            cooldown = 90
        )
        AtelierHapticEvent.AddToPalette,
        AtelierHapticEvent.GalleryImport -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.50f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.82f, delayMs = 50),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 124
        )
        AtelierHapticEvent.Undo -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.36f),
            primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.66f, delayMs = 45),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 88,
            cooldown = 90
        )
        AtelierHapticEvent.Save,
        AtelierHapticEvent.Favorite -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.58f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.92f, delayMs = 60),
            fallback = VibrationEffect.EFFECT_DOUBLE_CLICK,
            amplitude = 156,
            duration = 20,
            cooldown = 100
        )
        AtelierHapticEvent.Delete -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.62f),
            primitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.86f, delayMs = 50),
            fallback = VibrationEffect.EFFECT_HEAVY_CLICK,
            amplitude = 168,
            duration = 20,
            cooldown = 120
        )
        AtelierHapticEvent.Duplicate -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.48f),
            primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.72f, delayMs = 55),
            fallback = VibrationEffect.EFFECT_DOUBLE_CLICK,
            amplitude = 96
        )
        AtelierHapticEvent.Reorder -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.34f),
            fallback = VibrationEffect.EFFECT_TICK,
            amplitude = 34,
            cooldown = 48
        )
        AtelierHapticEvent.ReorderDrop -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.70f),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 92,
            cooldown = 80
        )
        AtelierHapticEvent.Share,
        AtelierHapticEvent.Export -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.48f),
            primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.86f, delayMs = 55),
            fallback = VibrationEffect.EFFECT_CLICK,
            amplitude = 132
        )
        AtelierHapticEvent.Swipe -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.42f),
            fallback = VibrationEffect.EFFECT_TICK,
            amplitude = 42,
            cooldown = 70
        )
        AtelierHapticEvent.Search -> phrase(
            primitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.28f),
            fallback = VibrationEffect.EFFECT_TICK,
            amplitude = 28,
            cooldown = 110
        )
    }

    private fun phrase(
        vararg primitives: PrimitiveStep,
        fallback: Int,
        amplitude: Int,
        duration: Long = 12,
        cooldown: Long = 70
    ) = HapticPhrase(
        primitives = primitives.toList(),
        fallbackEffect = fallback,
        legacyAmplitude = amplitude,
        legacyDurationMs = duration,
        cooldownMs = cooldown
    )

    private fun primitive(primitiveId: Int, scale: Float, delayMs: Int = 0) =
        PrimitiveStep(primitiveId = primitiveId, scale = scale, delayMs = delayMs)

    private data class PrimitiveStep(
        val primitiveId: Int,
        val scale: Float,
        val delayMs: Int
    )

    private data class HapticPhrase(
        val primitives: List<PrimitiveStep>,
        val fallbackEffect: Int,
        val legacyAmplitude: Int,
        val legacyDurationMs: Long,
        val cooldownMs: Long
    )
}

internal class HapticRateLimiter(
    private val clock: () -> Long
) {
    private val lastPlayedAt = mutableMapOf<AtelierHapticEvent, Long>()

    fun tryAcquire(event: AtelierHapticEvent, cooldownMs: Long): Boolean {
        val now = clock()
        synchronized(lastPlayedAt) {
            val elapsed = now - (lastPlayedAt[event] ?: Long.MIN_VALUE)
            if (elapsed in 0 until cooldownMs) return false
            lastPlayedAt[event] = now
            return true
        }
    }
}

@Composable
fun rememberAtelierHaptics(enabled: Boolean): AtelierHaptics {
    val context = LocalContext.current
    val latestEnabled = rememberUpdatedState(enabled)
    return remember(context) {
        AtelierHaptics(context) { latestEnabled.value }
    }
}
