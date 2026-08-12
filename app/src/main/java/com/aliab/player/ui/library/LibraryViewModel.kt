package com.aliab.player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliab.player.data.media.MediaStoreRepository
import com.aliab.player.data.settings.SettingsRepository
import com.aliab.player.data.settings.SongSort
import com.aliab.player.model.Album
import com.aliab.player.model.Artist
import com.aliab.player.model.Folder
import com.aliab.player.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Loads the MediaStore catalog once on creation and derives all library sections from it, so a
 * large library never triggers repeated full queries. The songs list re-sorts in memory whenever
 * the user changes the sort preference.
 */
class LibraryViewModel(
    private val mediaStoreRepository: MediaStoreRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _themeMode = MutableStateFlow(com.aliab.player.data.settings.ThemeMode.SYSTEM)
    val themeMode: StateFlow<com.aliab.player.data.settings.ThemeMode> = _themeMode.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _folderSongs = MutableStateFlow<List<Song>>(emptyList())
    val folderSongs: StateFlow<List<Song>> = _folderSongs.asStateFlow()

    private val _folderLoading = MutableStateFlow(false)
    val folderLoading: StateFlow<Boolean> = _folderLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoriteSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteSongIds: StateFlow<Set<Long>> = _favoriteSongIds.asStateFlow()

    private val _currentSort = MutableStateFlow(SongSort.TITLE)
    val currentSort: StateFlow<SongSort> = _currentSort.asStateFlow()

    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            settingsRepository.toggleFavorite(songId)
        }
    }

    fun setSongSort(sort: SongSort, ascending: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSongSort(sort, ascending)
        }
    }

    fun setTheme(mode: com.aliab.player.data.settings.ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun saveQueue(songIds: List<Long>, index: Int, positionMs: Long) {
        viewModelScope.launch {
            settingsRepository.saveLastQueue(songIds, index, positionMs)
        }
    }

    suspend fun getLastQueueState(): Triple<List<Long>, Int, Long>? {
        val settings = settingsRepository.settings.first()
        if (!settings.restorePlaybackOnLaunch || settings.lastQueueSongIds.isEmpty()) return null
        return Triple(settings.lastQueueSongIds, settings.lastQueueIndex, settings.lastQueuePositionMs)
    }

    private var latestCatalog: List<Song> = emptyList()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _themeMode.value = settings.themeMode
                _favoriteSongIds.value = settings.favoriteSongIds
                _currentSort.value = settings.songSort
                _isAscending.value = settings.songsSortAscending
                // Always re-sort the most recent catalog so a settings change can't resurrect
                // songs deleted since the last scan.
                _songs.value = latestCatalog.sortedWith(
                    songComparator(settings.songSort, settings.songsSortAscending),
                )
            }
        }
        viewModelScope.launch { _folders.value = mediaStoreRepository.queryFolders() }
        refresh()
    }

    /**
     * Re-scans the MediaStore catalog (e.g. after the user deletes a song) and repopulates every
     * library section from the single query.
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            latestCatalog = mediaStoreRepository.querySongs()
            val (albums, artists) = withContext(Dispatchers.Default) {
                mediaStoreRepository.albumsFrom(latestCatalog) to
                    mediaStoreRepository.artistsFrom(latestCatalog)
            }
            _albums.value = albums
            _artists.value = artists
            val settings = settingsRepository.settings.first()
            _songs.value = latestCatalog.sortedWith(
                songComparator(settings.songSort, settings.songsSortAscending),
            )
            _isLoading.value = false
        }
    }

    /** Every track credited to an artist, from the already-loaded catalog. */
    fun songsForArtist(artistName: String): List<Song> =
        _songs.value.filter { it.artist == artistName }.sortedBy { it.title.lowercase() }

    /** Loads the tracks of one folder. Folder membership lives only in MediaStore. */
    fun loadFolderSongs(folderPath: String) {
        viewModelScope.launch {
            _folderLoading.value = true
            _folderSongs.value = mediaStoreRepository.querySongsInFolder(folderPath)
            _folderLoading.value = false
        }
    }

    private fun songComparator(sort: SongSort, ascending: Boolean): Comparator<Song> {
        // Explicit type parameter avoids the ambiguous compareBy(comparator, ...) overloads.
        val comparator: Comparator<Song> = when (sort) {
            SongSort.TITLE -> compareBy { it.title.lowercase() }
            SongSort.ARTIST -> compareBy<Song> { it.artist.lowercase() }
                .thenBy { it.title.lowercase() }
            SongSort.ALBUM -> compareBy<Song> { it.album.lowercase() }
                .thenBy { it.trackNumber ?: Int.MAX_VALUE }
                .thenBy { it.title.lowercase() }
            SongSort.DATE_ADDED -> compareBy { it.dateModified }
        }
        return if (ascending) comparator else comparator.reversed()
    }
}
