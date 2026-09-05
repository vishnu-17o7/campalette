package com.vishnu.campalette.ui

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Emits a value only when the discrete tick actually changes.
 * Pointer-hold MOVE spam and stale composed callbacks must not retrigger side effects.
 */
internal class DiscreteTickGate(initial: Int) {
    var last: Int = initial
        private set

    fun reset(value: Int) {
        last = value
    }

    fun offer(next: Int): Int? {
        if (next == last) return null
        last = next
        return next
    }

    fun offerRaw(raw: Float, min: Int, max: Int, hysteresis: Float = 0.12f): Int? {
        val nearest = PaletteSizeSliderMath.tick(raw, min, max)
        if (nearest == last) return null
        val threshold = 0.5f + hysteresis
        val crossed = if (nearest > last) raw >= last + threshold else raw <= last - threshold
        if (!crossed) return null
        last = nearest
        return nearest
    }
}

internal object PaletteSizeSliderMath {
    fun tick(raw: Float, min: Int, max: Int): Int =
        raw.roundToInt().coerceIn(min, max)

    fun thumbCenterX(
        count: Int,
        min: Int,
        max: Int,
        width: Float,
        thumbRadius: Float
    ): Float {
        val range = (max - min).toFloat().coerceAtLeast(1f)
        val inner = (width - thumbRadius * 2f).coerceAtLeast(1f)
        val fraction = ((count - min).toFloat() / range).coerceIn(0f, 1f)
        return thumbRadius + fraction * inner
    }

    fun isThumbGrab(x: Float, thumbCenterX: Float, thumbRadius: Float): Boolean =
        abs(x - thumbCenterX) <= thumbRadius * 1.5f

    fun rawFromX(
        x: Float,
        width: Float,
        thumbRadius: Float,
        min: Int,
        max: Int
    ): Float {
        val range = (max - min).toFloat()
        val inner = (width - thumbRadius * 2f).coerceAtLeast(1f)
        val t = ((x - thumbRadius) / inner).coerceIn(0f, 1f)
        return min + t * range
    }
}
