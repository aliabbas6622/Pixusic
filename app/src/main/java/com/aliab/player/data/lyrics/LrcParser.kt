package com.aliab.player.data.lyrics

import java.io.File
import java.nio.charset.Charset

data class LyricLine(
    val timeMs: Long,
    val text: String,
)

object LrcParser {

    // Matches [mm:ss], [mm:ss.x], [mm:ss.xx], [mm:ss.xxx] and the legacy [mm:ss:xx] variant.
    private val TIMESTAMP_REGEX = Regex("""\[(\d{1,2}):(\d{1,2})(?:[.:](\d{1,3}))?\]""")

    /**
     * Parses LRC content into ordered lines. A line may carry several timestamps
     * (`[00:12.00][00:45.00]chorus`) — each one produces its own [LyricLine]. Metadata tags
     * (`[ar:]`, `[ti:]`, …) and untimestamped text are ignored.
     */
    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        lrcContent.lines().forEach { rawLine ->
            val matches = TIMESTAMP_REGEX.findAll(rawLine).toList()
            if (matches.isNotEmpty()) {
                // Lyric text is everything after the last timestamp; on a multi-timestamp line
                // ([00:12.00][00:45.00]chorus) it is the same for every timestamp.
                val text = rawLine.replace(TIMESTAMP_REGEX, "").trim()
                matches.forEach { match ->
                    val min = match.groupValues[1].toLongOrNull() ?: 0L
                    val sec = match.groupValues[2].toLongOrNull() ?: 0L
                    val fraction = match.groupValues[3]
                    val millis = when {
                        fraction.isEmpty() -> 0L
                        fraction.length == 1 -> fraction.toLong() * 100
                        fraction.length == 2 -> fraction.toLong() * 10
                        else -> fraction.toLong()
                    }
                    val timeMs = (min * 60 + sec) * 1000 + millis
                    lines.add(LyricLine(timeMs, text))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    /**
     * Looks for a matching `.lrc` file alongside the given audio file path. The exact-case name
     * is tried first; then a case-insensitive scan of the directory (for `.LRC` on case-sensitive
     * filesystems).
     */
    fun findLrcFile(audioFilePath: String): File? {
        val audioFile = File(audioFilePath)
        if (!audioFile.exists()) return null
        val dir = audioFile.parentFile ?: return null
        val baseName = audioFile.nameWithoutExtension

        val exact = File(dir, "$baseName.lrc")
        if (exact.exists()) return exact

        return dir.listFiles { f -> f.isFile && f.extension.equals("lrc", ignoreCase = true) }
            ?.firstOrNull { it.nameWithoutExtension.equals(baseName, ignoreCase = true) }
    }

    /**
     * Reads an LRC file as UTF-8 (BOM-safe), falling back to GBK when the bytes are not valid
     * UTF-8 (common for CJK lyrics). Returns null on I/O failure.
     */
    fun readLrcContent(file: File): String? = runCatching {
        val bytes = file.readBytes()
        val utf8 = bytes.toString(Charsets.UTF_8).trimStart('\uFEFF')
        if (utf8.contains('\uFFFD')) {
            bytes.toString(Charset.forName("GBK")).trimStart('\uFEFF')
        } else {
            utf8
        }
    }.getOrNull()
}
