package com.vishnu.campalette.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteSizeTicksTest {
    @Test
    fun gateIgnoresRepeatsOfTheSameTick() {
        val gate = DiscreteTickGate(7)
        val emitted = mutableListOf<Int>()

        repeat(12) {
            gate.offer(7)?.let(emitted::add)
        }

        assertEquals(emptyList<Int>(), emitted)
        assertEquals(7, gate.last)
    }

    @Test
    fun gateEmitsOnlyWhenTheIntegerChanges() {
        val gate = DiscreteTickGate(4)
        val emitted = mutableListOf<Int>()

        listOf(4, 4, 5, 5, 5, 6, 6, 5).forEach { value ->
            gate.offer(value)?.let(emitted::add)
        }

        assertEquals(listOf(5, 6, 5), emitted)
    }

    @Test
    fun holdOnThumbDoesNotJumpTheTick() {
        val min = 2
        val max = 10
        val width = 320f
        val thumbRadius = 14f
        val count = 6
        val thumbX = PaletteSizeSliderMath.thumbCenterX(count, min, max, width, thumbRadius)

        assertTrue(PaletteSizeSliderMath.isThumbGrab(thumbX, thumbX, thumbRadius))
        assertTrue(PaletteSizeSliderMath.isThumbGrab(thumbX + thumbRadius, thumbX, thumbRadius))
        assertEquals(count, PaletteSizeSliderMath.tick(count.toFloat(), min, max))
    }

    @Test
    fun tapOnTrackMapsToADifferentTick() {
        val min = 2
        val max = 10
        val width = 320f
        val thumbRadius = 14f
        val count = 6
        val thumbX = PaletteSizeSliderMath.thumbCenterX(count, min, max, width, thumbRadius)
        val tapX = width - thumbRadius

        assertFalse(PaletteSizeSliderMath.isThumbGrab(tapX, thumbX, thumbRadius))
        val tapped = PaletteSizeSliderMath.tick(
            PaletteSizeSliderMath.rawFromX(tapX, width, thumbRadius, min, max),
            min,
            max
        )
        assertEquals(max, tapped)
        assertNull(DiscreteTickGate(count).offer(count))
        assertEquals(max, DiscreteTickGate(count).offer(tapped))
    }

    @Test
    fun holdJitterOnATickBoundaryDoesNotRetrigger() {
        val gate = DiscreteTickGate(6)
        val emitted = mutableListOf<Int>()
        listOf(6.48f, 6.51f, 6.49f, 6.50f, 6.52f).forEach { raw ->
            gate.offerRaw(raw, min = 2, max = 10)?.let(emitted::add)
        }
        assertEquals(emptyList<Int>(), emitted)

        assertEquals(7, gate.offerRaw(6.70f, min = 2, max = 10))
        assertNull(gate.offerRaw(6.70f, min = 2, max = 10))
        assertNull(gate.offerRaw(6.55f, min = 2, max = 10))
        assertEquals(6, gate.offerRaw(6.30f, min = 2, max = 10))
    }
}
