package com.vishnu.campalette.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticRateLimiterTest {
    @Test
    fun firstEventIsAllowedThenSuppressedUntilCooldownBoundary() {
        var now = 1_000L
        val limiter = HapticRateLimiter { now }

        assertTrue(limiter.tryAcquire(AtelierHapticEvent.Navigation, cooldownMs = 55L))

        now = 1_054L
        assertFalse(limiter.tryAcquire(AtelierHapticEvent.Navigation, cooldownMs = 55L))

        now = 1_055L
        assertTrue(limiter.tryAcquire(AtelierHapticEvent.Navigation, cooldownMs = 55L))
    }

    @Test
    fun cooldownsAreIndependentPerEvent() {
        var now = 2_000L
        val limiter = HapticRateLimiter { now }

        assertTrue(limiter.tryAcquire(AtelierHapticEvent.Sample, cooldownMs = 55L))
        assertFalse(limiter.tryAcquire(AtelierHapticEvent.Sample, cooldownMs = 55L))
        assertTrue(limiter.tryAcquire(AtelierHapticEvent.Reorder, cooldownMs = 48L))

        now = 2_048L
        assertFalse(limiter.tryAcquire(AtelierHapticEvent.Sample, cooldownMs = 55L))
        assertTrue(limiter.tryAcquire(AtelierHapticEvent.Reorder, cooldownMs = 48L))
    }
}
