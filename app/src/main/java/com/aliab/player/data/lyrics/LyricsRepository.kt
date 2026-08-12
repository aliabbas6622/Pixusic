package com.aliab.player.data.lyrics

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Looks for an `.lrc` file alongside the current song and parses it into ordered lines. Returns
 * null when no lyrics were found, so the caller can fall back to a placeholder.
 */
class LyricsRepository(context: Context) {

    private val resolver = context.applicationContext.contentResolver

    suspend fun load(songUri: Uri): List<LyricLine>? = withContext(Dispatchers.IO) {
        val audioPath = resolveFilePath(songUri) ?: return@withContext null
        val lrcFile = LrcParser.findLrcFile(audioPath) ?: return@withContext null
        val content = LrcParser.readLrcContent(lrcFile) ?: return@withContext null
        LrcParser.parse(content).takeIf { it.isNotEmpty() }
    }

    private fun resolveFilePath(uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        // content://media/external/audio/<id> — DATA column carries the real filesystem path on
        // every supported API level.
        return resolver.query(
            uri,
            arrayOf(MediaStore.MediaColumns.DATA),
            null,
            null,
            null,
        )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }
}
