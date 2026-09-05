package com.vishnu.campalette.data

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.ui.AtelierData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampaletteRepositoryThemeTest {
    @Test
    fun themeModePersistsAndInvalidValuesMigrateToSystem() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("campalette", Context.MODE_PRIVATE)
        val hadStoredValue = preferences.contains(THEME_MODE_KEY)
        val storedValue = preferences.getString(THEME_MODE_KEY, null)

        try {
            val repository = CampaletteRepository(context)
            repository.setThemeMode(ThemeMode.Dark)
            assertEquals(ThemeMode.Dark, repository.loadSettings().themeMode)

            preferences.edit().putString(THEME_MODE_KEY, "invalid").commit()
            assertEquals(ThemeMode.System, repository.loadSettings().themeMode)
        } finally {
            val editor = preferences.edit()
            if (hadStoredValue) editor.putString(THEME_MODE_KEY, storedValue) else editor.remove(THEME_MODE_KEY)
            editor.commit()
        }
    }

    @Test
    fun paletteLibraryAndSettingsRoundTripWithoutDataLoss() = runBlocking {
        withRestoredPreferences { repository, preferences ->
            assertTrue(preferences.edit().clear().commit())
            val studies = listOf(
                PaletteStudy(
                    name = "Forest",
                    colors = listOf(
                        PaletteColor(
                            name = "Pine",
                            color = 0xFF163A24.toInt(),
                            hexCode = "#163A24",
                            red = 22,
                            green = 58,
                            blue = 36
                        ),
                        PaletteColor(
                            name = "Moss",
                            color = 0xFF789262.toInt(),
                            hexCode = "#789262",
                            red = 120,
                            green = 146,
                            blue = 98
                        )
                    ),
                    note = "Outdoor study",
                    capturedAt = "17 Jul, 13:30",
                    source = "Live Camera"
                )
            )
            val expectedLibrary = PaletteLibrarySnapshot(
                saved = studies,
                history = studies.map { it.copy(name = "Forest history") }
            )

            repository.saveLibrary(expectedLibrary)
            repository.setHapticsEnabled(false)
            repository.setPaletteTintEnabled(false)
            repository.setReducedMotion(true)
            repository.setThemeMode(ThemeMode.Dark)
            repository.setPaletteColorCount(4)

            assertEquals(expectedLibrary, repository.loadLibrary())
            assertEquals(
                SettingsSnapshot(
                    hapticsEnabled = false,
                    paletteTintEnabled = false,
                    reducedMotion = true,
                    themeMode = ThemeMode.Dark,
                    paletteColorCount = 4
                ),
                repository.loadSettings()
            )
        }
    }

    @Test
    fun paletteColorCountClampsToSupportedRange() = runBlocking {
        withRestoredPreferences { repository, preferences ->
            assertTrue(preferences.edit().clear().commit())

            repository.setPaletteColorCount(99)
            assertEquals(AtelierData.MAX_PALETTE_COLOR_COUNT, repository.loadSettings().paletteColorCount)

            repository.setPaletteColorCount(-5)
            assertEquals(AtelierData.MIN_PALETTE_COLOR_COUNT, repository.loadSettings().paletteColorCount)
        }
    }

    @Test
    fun malformedPaletteStorageFallsBackToEmptyLibrary() = runBlocking {
        withRestoredPreferences { repository, preferences ->
            assertTrue(
                preferences.edit()
                    .putString("saved_json", "not-json")
                    .putString("history_json", "{")
                    .commit()
            )

            assertEquals(PaletteLibrarySnapshot(emptyList(), emptyList()), repository.loadLibrary())
        }
    }

    private suspend fun withRestoredPreferences(
        block: suspend (CampaletteRepository, SharedPreferences) -> Unit
    ) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("campalette", Context.MODE_PRIVATE)
        val original = preferences.all.toMap()
        try {
            block(CampaletteRepository(context), preferences)
        } finally {
            val editor = preferences.edit().clear()
            original.forEach { (key, value) ->
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is String -> editor.putString(key, value)
                    is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                }
            }
            assertTrue(editor.commit())
        }
    }

    private companion object {
        const val THEME_MODE_KEY = "theme_mode"
    }
}
