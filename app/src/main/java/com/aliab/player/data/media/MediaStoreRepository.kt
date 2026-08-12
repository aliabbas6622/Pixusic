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
 * Reads the local audio catalog from MediaStore. The library filters out short audio clips
 * (<10s), WhatsApp Voice Notes/Audio/Documents, Ringtones, Notifications, and Voice Recordings.
 */
class MediaStoreRepository(context: Context) {

    private val resolver = context.applicationContext.contentResolver

    /** All playable music tracks, sorted by title. User-facing sorting happens in the UI layer. */
    suspend fun querySongs(): List<Song> = querySongs(
        selection = IS_MUSIC_SELECTION,
        selectionArgs = arrayOf(MIN_DURATION_MS.toString()),
        sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
    )

    /** Every track inside a folder (matched by MediaStore relative path). */
    suspend fun querySongsInFolder(folderPath: String): List<Song> = querySongs(
        selection = "$IS_MUSIC_SELECTION AND ${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?",
        selectionArgs = arrayOf(MIN_DURATION_MS.toString(), "$folderPath/%"),
        sortOrder = TITLE_SORT,
    )

    /**
     * Albums grouped from an already-loaded song catalog. Deriving from the single [querySongs]
     * result avoids re-scanning MediaStore for every library tab.
     */
    fun albumsFrom(songs: List<Song>): List<Album> =
        songs.groupBy { it.albumId.coerceAtLeast(0L) }
            .map { (albumId, tracks) ->
                val first = tracks.first()
                Album(
                    id = albumId,
                    name = first.album,
                    artist = first.artist,
                    year = tracks.mapNotNull { it.year }.maxOrNull(),
                    songCount = tracks.size,
                )
            }
            .filterNot { isExcludedName(it.name) }
            .sortedBy { it.name.lowercase() }

    /** Artists grouped from an already-loaded song catalog. */
    fun artistsFrom(songs: List<Song>): List<Artist> =
        songs.groupBy { it.artist }
            .map { (artist, tracks) -> Artist(name = artist, songCount = tracks.size) }
            .filterNot { isExcludedName(it.name) }
            .sortedBy { it.name.lowercase() }

    /** Top-level folders that contain music, with track counts, from MediaStore relative paths. */
    suspend fun queryFolders(): List<Folder> = withContext(Dispatchers.IO) {
        val counts = LinkedHashMap<String, Int>()
        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.RELATIVE_PATH),
            IS_MUSIC_SELECTION,
            arrayOf(MIN_DURATION_MS.toString()),
            "${MediaStore.Audio.Media.RELATIVE_PATH} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            while (cursor.moveToNext()) {
                val folderPath = cursor.getString(pathIndex)
                    ?.trimEnd('/')
                    ?.takeIf { it.isNotBlank() }
                    ?: continue

                if (isExcludedPath(folderPath)) continue

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
            val relativePath = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {
                val path = if (relativePath >= 0) cursor.getString(relativePath).orEmpty() else ""
                val albumName = cursor.getString(album).orEmpty()
                val songTitle = cursor.getString(title).orEmpty()

                if (isExcludedPath(path) || isExcludedName(albumName) || isExcludedName(songTitle)) {
                    continue
                }

                val songId = cursor.getLong(id)
                val songDuration = cursor.getLong(duration).coerceAtLeast(0L)
                if (songDuration < MIN_DURATION_MS) continue

                result += Song(
                    id = songId,
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId),
                    title = songTitle.toDisplayString(UNKNOWN_TITLE),
                    artist = cursor.getString(artist).orEmpty().toDisplayString(UNKNOWN_ARTIST),
                    album = albumName.toDisplayString(UNKNOWN_ALBUM),
                    albumId = cursor.getLong(albumId),
                    durationMs = songDuration,
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

    private fun String.toDisplayString(fallback: String): String =
        if (isBlank() || equals(MediaStore.UNKNOWN_STRING, ignoreCase = true)) fallback else this

    private fun isExcludedPath(path: String): Boolean {
        val lower = path.lowercase()
        return EXCLUDED_KEYWORDS.any { lower.contains(it) }
    }

    private fun isExcludedName(name: String): Boolean {
        val lower = name.lowercase()
        return EXCLUDED_KEYWORDS.any { lower.contains(it) }
    }

    private companion object {
        const val UNKNOWN_TITLE = "Unknown title"
        const val UNKNOWN_ARTIST = "Unknown artist"
        const val UNKNOWN_ALBUM = "Unknown album"
        const val MIN_DURATION_MS = 10_000L // Exclude short audio clips under 10 seconds

        const val IS_MUSIC_SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        const val TITLE_SORT = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val EXCLUDED_KEYWORDS = listOf(
            "whatsapp",
            "voice note",
            "voicenote",
            "voice_note",
            "call_rec",
            "callrecord",
            "recordings",
            "notification",
            "ringtone",
            "alarm",
        )

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
            MediaStore.Audio.Media.RELATIVE_PATH,
        )
    }
}
