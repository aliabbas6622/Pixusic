package com.aliab.player.data.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.aliab.player.model.Album
import com.aliab.player.model.Artist
import com.aliab.player.model.Folder
import com.aliab.player.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the local audio catalog from MediaStore. The library is a live query result, not a copy:
 * nothing here is persisted, and the app never mirrors the full catalog into Room.
 */
class MediaStoreRepository(context: Context) {

    private val resolver = context.applicationContext.contentResolver

    /** All playable music tracks, sorted by title. User-facing sorting happens in the UI layer. */
    suspend fun querySongs(): List<Song> = querySongs(
        selection = IS_MUSIC_SELECTION,
        selectionArgs = null,
        sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
    )

    /** Every track inside a folder (matched by MediaStore relative path). */
    suspend fun querySongsInFolder(folderPath: String): List<Song> = querySongs(
        selection = "$IS_MUSIC_SELECTION AND ${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?",
        selectionArgs = arrayOf("$folderPath/%"),
        sortOrder = TITLE_SORT,
    )

    /** Albums grouped from the catalog with track counts; works on every supported API level. */
    suspend fun queryAlbums(): List<Album> = withContext(Dispatchers.IO) {
        querySongsCore(null, TITLE_SORT)
            // MediaStore reports untagged files with ALBUM_ID 0 or -1; merge them into one
            // "Unknown album" group so they don't appear as two separate cards.
            .groupBy { it.albumId.coerceAtLeast(0L) }
            .map { (albumId, songs) ->
                val first = songs.first()
                Album(
                    id = albumId,
                    name = first.album,
                    artist = first.artist,
                    year = songs.mapNotNull { it.year }.maxOrNull(),
                    songCount = songs.size,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    /** Artists grouped from the catalog with track counts. */
    suspend fun queryArtists(): List<Artist> = withContext(Dispatchers.IO) {
        querySongsCore(null, TITLE_SORT)
            .groupBy { it.artist }
            .map { (artist, songs) -> Artist(name = artist, songCount = songs.size) }
            .sortedBy { it.name.lowercase() }
    }

    /** Top-level folders that contain music, with track counts, from MediaStore relative paths. */
    suspend fun queryFolders(): List<Folder> = withContext(Dispatchers.IO) {
        val counts = LinkedHashMap<String, Int>()
        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.RELATIVE_PATH),
            IS_MUSIC_SELECTION,
            null,
            "${MediaStore.Audio.Media.RELATIVE_PATH} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            while (cursor.moveToNext()) {
                val folderPath = cursor.getString(pathIndex)
                    ?.trimEnd('/')
                    ?.takeIf { it.isNotBlank() }
                    ?: continue
                counts[folderPath] = (counts[folderPath] ?: 0) + 1
            }
        }
        counts.map { (path, count) ->
            Folder(path = path, displayName = path.substringAfterLast('/'), songCount = count)
        }.sortedBy { it.displayName.lowercase() }
    }

    private suspend fun querySongs(
        selection: String,
        selectionArgs: Array<String>?,
        sortOrder: String,
    ): List<Song> = withContext(Dispatchers.IO) {
        querySongsCore(selection, sortOrder, selectionArgs)
    }

    private fun querySongsCore(
        selection: String?,
        sortOrder: String,
        selectionArgs: Array<String>? = null,
    ): List<Song> {
        val result = mutableListOf<Song>()
        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            SONG_PROJECTION,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val track = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val year = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val mime = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateModified = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val songId = cursor.getLong(id)
                result += Song(
                    id = songId,
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId),
                    title = cursor.getString(title).orEmpty().toDisplayString(UNKNOWN_TITLE),
                    artist = cursor.getString(artist).orEmpty().toDisplayString(UNKNOWN_ARTIST),
                    album = cursor.getString(album).orEmpty().toDisplayString(UNKNOWN_ALBUM),
                    albumId = cursor.getLong(albumId),
                    durationMs = cursor.getLong(duration).coerceAtLeast(0L),
                    // MediaStore stores the track number multiplied by 1000 (e.g. 3000 = track 3).
                    trackNumber = cursor.getIntOrNull(track)?.div(1000)?.takeIf { it > 0 },
                    year = cursor.getIntOrNull(year),
                    mimeType = cursor.getString(mime),
                    dateModified = cursor.getLong(dateModified),
                )
            }
        }
        return result
    }

    private fun Cursor.getIntOrNull(index: Int): Int? = if (isNull(index)) null else getInt(index)

    /** MediaStore uses the literal "<unknown>" string for missing metadata; show a friendly label instead. */
    private fun String.toDisplayString(fallback: String): String =
        if (isBlank() || equals(MediaStore.UNKNOWN_STRING, ignoreCase = true)) fallback else this

    private companion object {
        const val UNKNOWN_TITLE = "Unknown title"
        const val UNKNOWN_ARTIST = "Unknown artist"
        const val UNKNOWN_ALBUM = "Unknown album"

        const val IS_MUSIC_SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        const val TITLE_SORT = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val SONG_PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )
    }
}
