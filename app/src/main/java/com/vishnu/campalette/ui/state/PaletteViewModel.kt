package com.vishnu.campalette.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.data.CampaletteRepository
import com.vishnu.campalette.data.PaletteLibrarySnapshot
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.HarmonyMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class PaletteWorkspaceState(
    val palette: List<PaletteColor> = emptyList(),
    val name: String = "",
    val source: String = "",
    val selectedHex: String? = null,
    val harmonyMode: HarmonyMode = HarmonyMode.Analogous,
    val latestCapturedStudy: PaletteStudy? = null
)

data class PendingStudyDeletion(
    val study: PaletteStudy,
    val savedIndex: Int,
    val historyIndex: Int
)

data class PendingColorDeletion(
    val color: PaletteColor,
    val index: Int
)

data class PaletteLibraryUiState(
    val palettes: List<PaletteStudy> = emptyList(),
    val savedPalettes: List<PaletteStudy> = emptyList(),
    val historyPalettes: List<PaletteStudy> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val savedColorHexes: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedFilter: String = "all",
    val isGridView: Boolean = false,
    val isLoaded: Boolean = false
)

private data class LibraryStorageState(
    val saved: List<PaletteStudy> = emptyList(),
    val history: List<PaletteStudy> = emptyList(),
    val isLoaded: Boolean = false
)

private data class LibraryControlsState(
    val searchQuery: String = "",
    val selectedFilter: String = "all",
    val isGridView: Boolean = false
)

class PaletteViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val repository = CampaletteRepository(application)
    private val libraryReady = CompletableDeferred<Unit>()
    private val persistenceMutex = Mutex()
    private var workspacePersistJob: Job? = null
    private var pendingStudyDeletion: PendingStudyDeletion? = null
    private var pendingColorDeletion: PendingColorDeletion? = null

    private val _workspace = MutableStateFlow(restoreWorkspace(savedStateHandle))
    val workspace: StateFlow<PaletteWorkspaceState> = _workspace

    private val _libraryStorage = MutableStateFlow(LibraryStorageState())
    private val _libraryControls = MutableStateFlow(
        LibraryControlsState(
            searchQuery = savedStateHandle.get<String>(KEY_SEARCH).orEmpty(),
            selectedFilter = savedStateHandle.get<String>(KEY_FILTER) ?: "all",
            isGridView = savedStateHandle.get<Boolean>(KEY_GRID) ?: false
        )
    )

    val library: StateFlow<PaletteLibraryUiState> = combine(
        _libraryStorage,
        _libraryControls
    ) { storage, controls ->
        val savedNames = storage.saved.mapTo(mutableSetOf()) { it.name }
        val visible = (storage.saved + storage.history)
            .distinctBy(PaletteStudy::libraryIdentity)
            .filter { study ->
                controls.searchQuery.isBlank() ||
                    study.name.contains(controls.searchQuery, ignoreCase = true) ||
                    study.colors.any { it.hexCode.contains(controls.searchQuery, ignoreCase = true) }
            }
            .filter { study ->
                when (controls.selectedFilter) {
                    "camera" -> study.source.contains("camera", ignoreCase = true) ||
                        study.source.contains("capture", ignoreCase = true)
                    "harmony" -> study.source.contains("harmony", ignoreCase = true)
                    "saved" -> study.name in savedNames
                    else -> true
                }
            }
        PaletteLibraryUiState(
            palettes = visible,
            savedPalettes = storage.saved,
            historyPalettes = storage.history,
            favorites = savedNames,
            savedColorHexes = storage.saved.asSequence()
                .filter { it.colors.size == 1 }
                .mapNotNull { it.colors.firstOrNull()?.hexCode }
                .toSet(),
            searchQuery = controls.searchQuery,
            selectedFilter = controls.selectedFilter,
            isGridView = controls.isGridView,
            isLoaded = storage.isLoaded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaletteLibraryUiState()
    )

    init {
        viewModelScope.launch {
            val snapshot = try {
                repository.loadLibrary()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                PaletteLibrarySnapshot(emptyList(), emptyList())
            }
            _libraryStorage.value = LibraryStorageState(
                saved = snapshot.saved,
                history = snapshot.history,
                isLoaded = true
            )
            libraryReady.complete(Unit)
        }
    }

    fun replaceWorkspace(
        palette: List<PaletteColor>,
        name: String,
        source: String,
        latestCapturedStudy: PaletteStudy? = _workspace.value.latestCapturedStudy
    ) {
        updateWorkspace {
            it.copy(
                palette = palette.uniqueColors(),
                name = name,
                source = source,
                selectedHex = palette.firstOrNull()?.hexCode,
                latestCapturedStudy = latestCapturedStudy
            )
        }
    }

    fun clearWorkspace() {
        updateWorkspace { PaletteWorkspaceState(harmonyMode = it.harmonyMode) }
    }

    fun setPalette(colors: List<PaletteColor>, source: String? = null) {
        val cleaned = colors.uniqueColors()
        updateWorkspace { current ->
            current.copy(
                palette = cleaned,
                source = source ?: current.source,
                selectedHex = current.selectedHex
                    ?.takeIf { selected -> cleaned.any { it.hexCode == selected } }
                    ?: cleaned.firstOrNull()?.hexCode
            )
        }
    }

    fun setPaletteName(name: String) = updateWorkspace { it.copy(name = name) }

    fun selectEditorColor(hex: String?) = updateWorkspace { it.copy(selectedHex = hex) }

    fun applyHarmony(mode: HarmonyMode, palette: List<PaletteColor>, source: String) {
        val cleaned = palette.uniqueColors()
        updateWorkspace {
            it.copy(
                palette = cleaned,
                source = source,
                selectedHex = cleaned.firstOrNull()?.hexCode,
                harmonyMode = mode
            )
        }
    }

    fun addColor(color: PaletteColor) {
        updateWorkspace { current ->
            if (current.palette.any { it.hexCode == color.hexCode }) {
                current.copy(selectedHex = color.hexCode)
            } else {
                current.copy(
                    palette = (current.palette + color).take(AtelierData.MAX_PALETTE_COLOR_COUNT),
                    selectedHex = color.hexCode
                )
            }
        }
    }

    fun removeColor(color: PaletteColor, keepAtLeastOne: Boolean = false) {
        val current = _workspace.value
        val index = current.palette.indexOfFirst { it.hexCode == color.hexCode }
        if (index < 0) return
        val remaining = current.palette.filterNot { it.hexCode == color.hexCode }
        if (keepAtLeastOne && remaining.isEmpty()) return
        pendingColorDeletion = PendingColorDeletion(color, index)
        updateWorkspace {
            it.copy(
                palette = remaining,
                selectedHex = it.selectedHex
                    ?.takeIf { selected -> remaining.any { color -> color.hexCode == selected } }
                    ?: remaining.firstOrNull()?.hexCode
            )
        }
    }

    fun undoRemoveColor() {
        val pending = pendingColorDeletion ?: return
        pendingColorDeletion = null
        updateWorkspace { current ->
            if (current.palette.any { it.hexCode == pending.color.hexCode }) {
                current
            } else {
                val restored = current.palette.toMutableList()
                restored.add(pending.index.coerceIn(0, restored.size), pending.color)
                current.copy(
                    palette = restored.take(AtelierData.MAX_PALETTE_COLOR_COUNT),
                    selectedHex = pending.color.hexCode
                )
            }
        }
    }

    fun reorderColor(from: Int, to: Int) {
        updateWorkspace { current ->
            if (from !in current.palette.indices || to !in current.palette.indices || from == to) {
                current
            } else {
                val reordered = current.palette.toMutableList()
                val item = reordered.removeAt(from)
                reordered.add(to, item)
                current.copy(palette = reordered)
            }
        }
    }

    fun setSearchQuery(query: String) {
        savedStateHandle[KEY_SEARCH] = query
        _libraryControls.value = _libraryControls.value.copy(searchQuery = query)
    }

    fun setLibraryFilter(filter: String) {
        savedStateHandle[KEY_FILTER] = filter
        _libraryControls.value = _libraryControls.value.copy(selectedFilter = filter)
    }

    fun toggleLibraryGrid() {
        val isGrid = !_libraryControls.value.isGridView
        savedStateHandle[KEY_GRID] = isGrid
        _libraryControls.value = _libraryControls.value.copy(isGridView = isGrid)
    }

    fun recordHistory(study: PaletteStudy) {
        mutateLibrary { current ->
            current.copy(
                history = (listOf(study) + current.history)
                    .distinctBy { "${it.capturedAt}|${it.libraryIdentity()}" }
                    .take(MAX_HISTORY_ITEMS)
            )
        }
    }

    fun saveStudy(study: PaletteStudy) {
        if (study.colors.isEmpty()) return
        val cleaned = study.copy(colors = study.colors.uniqueColors())
        mutateLibrary { current ->
            current.copy(
                saved = listOf(cleaned) + current.saved.filterNot { it.name == cleaned.name }
            )
        }
    }

    fun saveSingleColor(study: PaletteStudy) {
        val color = study.colors.singleOrNull() ?: return
        mutateLibrary { current ->
            current.copy(
                saved = listOf(study) + current.saved.filterNot {
                    it.colors.size == 1 && it.colors.firstOrNull()?.hexCode == color.hexCode
                }
            )
        }
    }

    fun deleteStudy(study: PaletteStudy) {
        val identity = study.libraryIdentity()
        val current = _libraryStorage.value
        pendingStudyDeletion = PendingStudyDeletion(
            study = study,
            savedIndex = current.saved.indexOfFirst { it.libraryIdentity() == identity },
            historyIndex = current.history.indexOfFirst { it.libraryIdentity() == identity }
        )
        mutateLibrary { storage ->
            storage.copy(
                saved = storage.saved.filterNot { it.libraryIdentity() == identity },
                history = storage.history.filterNot { it.libraryIdentity() == identity }
            )
        }
    }

    fun undoDeleteStudy() {
        val pending = pendingStudyDeletion ?: return
        pendingStudyDeletion = null
        mutateLibrary { current ->
            val saved = current.saved.toMutableList()
            val history = current.history.toMutableList()
            val identity = pending.study.libraryIdentity()
            if (pending.savedIndex >= 0 && saved.none { it.libraryIdentity() == identity }) {
                saved.add(pending.savedIndex.coerceIn(0, saved.size), pending.study)
            }
            if (pending.historyIndex >= 0 && history.none { it.libraryIdentity() == identity }) {
                history.add(pending.historyIndex.coerceIn(0, history.size), pending.study)
            }
            current.copy(saved = saved, history = history)
        }
    }

    private fun updateWorkspace(transform: (PaletteWorkspaceState) -> PaletteWorkspaceState) {
        val next = transform(_workspace.value)
        _workspace.value = next
        persistWorkspace(next)
    }

    private fun persistWorkspace(next: PaletteWorkspaceState) {
        workspacePersistJob?.cancel()
        workspacePersistJob = viewModelScope.launch {
            delay(WORKSPACE_PERSIST_DEBOUNCE_MS)
            val workspaceJson: String
            val latestJson: String?
            withContext(Dispatchers.Default) {
                workspaceJson = AtelierData.encodeStudyJson(
                    listOf(PaletteStudy(next.name, next.palette, source = next.source))
                )
                latestJson = next.latestCapturedStudy?.let { AtelierData.encodeStudyJson(listOf(it)) }
            }
            savedStateHandle[KEY_WORKSPACE] = workspaceJson
            savedStateHandle[KEY_SELECTED_HEX] = next.selectedHex
            savedStateHandle[KEY_HARMONY] = next.harmonyMode.name
            savedStateHandle[KEY_LATEST_STUDY] = latestJson
        }
    }

    private fun mutateLibrary(transform: (LibraryStorageState) -> LibraryStorageState) {
        viewModelScope.launch {
            libraryReady.await()
            val next = transform(_libraryStorage.value).copy(isLoaded = true)
            _libraryStorage.value = next
            persistenceMutex.withLock {
                repository.saveLibrary(PaletteLibrarySnapshot(next.saved, next.history))
            }
        }
    }

    private companion object {
        const val MAX_HISTORY_ITEMS = 100
        const val KEY_WORKSPACE = "palette_workspace"
        const val KEY_SELECTED_HEX = "palette_selected_hex"
        const val KEY_HARMONY = "palette_harmony"
        const val KEY_LATEST_STUDY = "palette_latest_study"
        const val KEY_SEARCH = "library_search"
        const val KEY_FILTER = "library_filter"
        const val KEY_GRID = "library_grid"
        const val WORKSPACE_PERSIST_DEBOUNCE_MS = 64L

        fun restoreWorkspace(handle: SavedStateHandle): PaletteWorkspaceState {
            val study = handle.get<String>(KEY_WORKSPACE)
                ?.let(AtelierData::decodeStudyJson)
                ?.firstOrNull()
            val latest = handle.get<String>(KEY_LATEST_STUDY)
                ?.let(AtelierData::decodeStudyJson)
                ?.firstOrNull()
            val mode = handle.get<String>(KEY_HARMONY)?.let { stored ->
                try {
                    HarmonyMode.valueOf(stored)
                } catch (_: IllegalArgumentException) {
                    null
                }
            } ?: HarmonyMode.Analogous
            return PaletteWorkspaceState(
                palette = study?.colors.orEmpty(),
                name = study?.name.orEmpty(),
                source = study?.source.orEmpty(),
                selectedHex = handle[KEY_SELECTED_HEX],
                harmonyMode = mode,
                latestCapturedStudy = latest
            )
        }
    }
}

private fun List<PaletteColor>.uniqueColors(): List<PaletteColor> =
    distinctBy(PaletteColor::color).take(AtelierData.MAX_PALETTE_COLOR_COUNT)

private fun PaletteStudy.libraryIdentity(): String = buildString {
    append(name)
    append('|')
    append(source)
    colors.forEach { append('|').append(it.hexCode) }
}
