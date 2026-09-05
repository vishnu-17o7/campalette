package com.vishnu.campalette.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.campalette.data.CampaletteRepository
import com.vishnu.campalette.data.SettingsSnapshot
import com.vishnu.campalette.data.ThemeMode
import com.vishnu.campalette.ui.AtelierData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SettingsUiState(
    val hapticsEnabled: Boolean = true,
    val paletteTintEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val paletteColorCount: Int = AtelierData.DEFAULT_PALETTE_COLOR_COUNT,
    val isLoaded: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CampaletteRepository(application)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val persistenceMutex = Mutex()
    private var hapticsTouched = false
    private var paletteTintTouched = false
    private var reducedMotionTouched = false
    private var themeModeTouched = false
    private var paletteColorCountTouched = false

    init {
        viewModelScope.launch {
            val stored = runCatching { repository.loadSettings() }
                .getOrElse { SettingsSnapshot() }
            val current = _uiState.value
            _uiState.value = SettingsUiState(
                hapticsEnabled = if (hapticsTouched) current.hapticsEnabled else stored.hapticsEnabled,
                paletteTintEnabled = if (paletteTintTouched) current.paletteTintEnabled else stored.paletteTintEnabled,
                reducedMotion = if (reducedMotionTouched) current.reducedMotion else stored.reducedMotion,
                themeMode = if (themeModeTouched) current.themeMode else stored.themeMode,
                paletteColorCount = if (paletteColorCountTouched) current.paletteColorCount else stored.paletteColorCount,
                isLoaded = true
            )
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        hapticsTouched = true
        _uiState.value = _uiState.value.copy(hapticsEnabled = enabled)
        viewModelScope.launch {
            persistenceMutex.withLock {
                runCatching { repository.setHapticsEnabled(enabled) }
            }
        }
    }

    fun setPaletteTintEnabled(enabled: Boolean) {
        paletteTintTouched = true
        _uiState.value = _uiState.value.copy(paletteTintEnabled = enabled)
        viewModelScope.launch {
            persistenceMutex.withLock {
                runCatching { repository.setPaletteTintEnabled(enabled) }
            }
        }
    }

    fun setReducedMotion(enabled: Boolean) {
        reducedMotionTouched = true
        _uiState.value = _uiState.value.copy(reducedMotion = enabled)
        viewModelScope.launch {
            persistenceMutex.withLock {
                runCatching { repository.setReducedMotion(enabled) }
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        themeModeTouched = true
        _uiState.value = _uiState.value.copy(themeMode = themeMode)
        viewModelScope.launch {
            persistenceMutex.withLock {
                runCatching { repository.setThemeMode(themeMode) }
            }
        }
    }

    fun setPaletteColorCount(count: Int) {
        val clamped = count.coerceIn(AtelierData.MIN_PALETTE_COLOR_COUNT, AtelierData.MAX_PALETTE_COLOR_COUNT)
        if (clamped == _uiState.value.paletteColorCount) return
        paletteColorCountTouched = true
        _uiState.value = _uiState.value.copy(paletteColorCount = clamped)
        viewModelScope.launch {
            persistenceMutex.withLock {
                runCatching { repository.setPaletteColorCount(clamped) }
            }
        }
    }
}
