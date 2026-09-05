package com.vishnu.campalette.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun storageValuesRoundTrip() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromStorageValue(mode.storageValue))
        }
    }

    @Test
    fun missingOrInvalidStorageFallsBackToSystem() {
        assertEquals(ThemeMode.System, ThemeMode.fromStorageValue(null))
        assertEquals(ThemeMode.System, ThemeMode.fromStorageValue("unknown"))
    }

    @Test
    fun systemModeTracksDeviceAppearance() {
        assertTrue(ThemeMode.System.resolve(systemInDarkTheme = true))
        assertFalse(ThemeMode.System.resolve(systemInDarkTheme = false))
    }

    @Test
    fun explicitModesIgnoreDeviceAppearance() {
        assertTrue(ThemeMode.Dark.resolve(systemInDarkTheme = false))
        assertFalse(ThemeMode.Light.resolve(systemInDarkTheme = true))
    }
}
