package com.aliab.player.model

import android.net.Uri

/**
 * A small, transport-safe description of an audio item from MediaStore.
 *
 * Artwork and playback objects deliberately do not belong here: the library can keep many of
 * these in memory without holding bitmaps, streams, or a player instance.
 */
data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val year: Int? = null,
    val mimeType: String? = null,
    val dateModified: Long = 0L,
)
