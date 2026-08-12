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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Loads the MediaStore catalog once on creation and derives all library sections from it, so a
 * large library never triggers repeated full queries. The songs list re-sorts in memory whenever
 * the user changes the sort preference.
 */
class LibraryViewModel(
    private val mediaStoreRepository: MediaStoreRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            val catalog = mediaStoreRepository.querySongs()
            _isLoading.value = false
            settingsRepository.settings.collect { settings ->
                _songs.value = catalog.sortedWith(songComparator(settings.songSort, settings.songsSortAscending))
            }
        }
        viewModelScope.launch { _albums.value = mediaStoreRepository.queryAlbums() }
        viewModelScope.launch { _artists.value = mediaStoreRepository.queryArtists() }
        viewModelScope.launch { _folders.value = mediaStoreRepository.queryFolders() }
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
