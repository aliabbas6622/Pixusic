package com.aliab.player.data.playlists

import com.aliab.player.model.Song
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val dao: PlaylistDao) {

    /** Live list of all playlists with their song counts. */
    val playlists: Flow<List<PlaylistWithSongCount>> = dao.observePlaylistsWithCount()

    suspend fun createPlaylist(name: String): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun renamePlaylist(id: Long, newName: String) {
        dao.getPlaylistById(id)?.let { dao.updatePlaylist(it.copy(name = newName.trim())) }
    }

    suspend fun deletePlaylist(id: Long) {
        dao.getPlaylistById(id)?.let { dao.deletePlaylist(it) }
    }

    /**
     * Returns the song IDs in order for a given playlist.
     * The caller looks them up in the library catalog.
     */
    suspend fun getSongIdsForPlaylist(playlistId: Long): List<Long> =
        dao.getSongsForPlaylist(playlistId).sortedBy { it.position }.map { it.songId }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val nextPosition = (dao.getMaxPosition(playlistId) ?: -1) + 1
        dao.insertSong(PlaylistSongEntity(playlistId = playlistId, songId = songId, position = nextPosition))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }
}
