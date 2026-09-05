package com.vishnu.campalette.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.ui.AtelierData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PaletteLibrarySnapshot(
    val saved: List<PaletteStudy>,
    val history: List<PaletteStudy>
)

data class SettingsSnapshot(
    val hapticsEnabled: Boolean = true,
    val paletteTintEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val paletteColorCount: Int = AtelierData.DEFAULT_PALETTE_COLOR_COUNT
)

/**
 * The single persistence boundary for small user-owned app state.
 *
 * JSON parsing and synchronous SharedPreferences commits stay off the main thread. A commit is
 * intentional here: ViewModels serialize writes and can therefore make ordering deterministic.
 */
@SuppressLint("ApplySharedPref")
class CampaletteRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    suspend fun loadLibrary(): PaletteLibrarySnapshot = withContext(Dispatchers.IO) {
        PaletteLibrarySnapshot(
            saved = preferences.getString(KEY_SAVED, null)
                ?.let(AtelierData::decodeStudyJson)
                .orEmpty(),
            history = preferences.getString(KEY_HISTORY, null)
                ?.let(AtelierData::decodeStudyJson)
                .orEmpty()
        )
    }

    suspend fun saveLibrary(snapshot: PaletteLibrarySnapshot) = withContext(Dispatchers.IO) {
        val savedJson = AtelierData.encodeStudyJson(snapshot.saved)
        val historyJson = AtelierData.encodeStudyJson(snapshot.history)
        preferences.edit(commit = true) {
            putString(KEY_SAVED, savedJson)
            putString(KEY_HISTORY, historyJson)
        }
        Unit
    }

    suspend fun loadSettings(): SettingsSnapshot = withContext(Dispatchers.IO) {
        SettingsSnapshot(
            hapticsEnabled = preferences.getBoolean(KEY_HAPTICS, true),
            paletteTintEnabled = preferences.getBoolean(KEY_PALETTE_TINT, true),
            reducedMotion = preferences.getBoolean(KEY_REDUCED_MOTION, false),
            themeMode = ThemeMode.fromStorageValue(preferences.getString(KEY_THEME_MODE, null)),
            paletteColorCount = preferences.getInt(KEY_PALETTE_COLOR_COUNT, AtelierData.DEFAULT_PALETTE_COLOR_COUNT)
                .coerceIn(AtelierData.MIN_PALETTE_COLOR_COUNT, AtelierData.MAX_PALETTE_COLOR_COUNT)
        )
    }

    suspend fun setHapticsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        preferences.edit(commit = true) { putBoolean(KEY_HAPTICS, enabled) }
        Unit
    }

    suspend fun setPaletteTintEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        preferences.edit(commit = true) { putBoolean(KEY_PALETTE_TINT, enabled) }
        Unit
    }

    suspend fun setReducedMotion(enabled: Boolean) = withContext(Dispatchers.IO) {
        preferences.edit(commit = true) { putBoolean(KEY_REDUCED_MOTION, enabled) }
        Unit
    }

    suspend fun setThemeMode(themeMode: ThemeMode) = withContext(Dispatchers.IO) {
        preferences.edit(commit = true) { putString(KEY_THEME_MODE, themeMode.storageValue) }
        Unit
    }

    suspend fun setPaletteColorCount(count: Int) = withContext(Dispatchers.IO) {
        val clamped = count.coerceIn(AtelierData.MIN_PALETTE_COLOR_COUNT, AtelierData.MAX_PALETTE_COLOR_COUNT)
        preferences.edit(commit = true) { putInt(KEY_PALETTE_COLOR_COUNT, clamped) }
        Unit
    }

    private companion object {
        const val PREFERENCES_NAME = "campalette"
        const val KEY_SAVED = "saved_json"
        const val KEY_HISTORY = "history_json"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_PALETTE_TINT = "palette_tint_enabled"
        const val KEY_REDUCED_MOTION = "reduced_motion"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_PALETTE_COLOR_COUNT = "palette_color_count"
    }
}
