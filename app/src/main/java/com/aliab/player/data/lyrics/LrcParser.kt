package com.aliab.player.data.lyrics

import java.io.File

data class LyricLine(
    val timeMs: Long,
    val text: String,
)

object LrcParser {
    private val TIMESTAMP_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\]""")

    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        lrcContent.lines().forEach { rawLine ->
            val match = TIMESTAMP_REGEX.find(rawLine)
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val fraction = match.groupValues[3]
                val millis = if (fraction.length == 2) fraction.toLong() * 10 else fraction.toLong()
                val timeMs = (min * 60 + sec) * 1000 + millis
                val text = rawLine.substring(match.range.last + 1).trim()
                lines.add(LyricLine(timeMs, text))
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    /**
     * Looks for a matching `.lrc` file alongside the given audio file path.
     */
    fun findLrcFile(audioFilePath: String): File? {
        val audioFile = File(audioFilePath)
        if (!audioFile.exists()) return null
        val lrcPath = audioFile.parentFile?.let { dir ->
            File(dir, audioFile.nameWithoutExtension + ".lrc")
        }
        return lrcPath?.takeIf { it.exists() }
    }
}
