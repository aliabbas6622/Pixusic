package com.aliab.player.ui

import java.util.Locale

/** "m:ss" for short tracks, "h:mm:ss" for long ones. */
internal fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000L
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

/** Cleans up raw file names (e.g. "4AM_in_Karachi_-_Talha_Anjum___...") into clean titles. */
internal fun formatDisplayName(rawText: String?): String {
    if (rawText.isNullOrBlank()) return "Unknown"
    var clean = rawText
    // Replace consecutive underscores with spaces if the string has no spaces
    if (!clean.contains(" ") && clean.contains("_")) {
        clean = clean.replace(Regex("_+"), " ").trim()
    }
    // Remove leading track numbers like "04. " or "04 - "
    clean = clean.replace(Regex("^[0-9]{1,2}[.\\s\\-_]+"), "")
    return clean.ifBlank { rawText }
}
