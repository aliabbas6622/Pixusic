package com.aliab.player.model

/** A group of songs sharing the same MediaStore album id. */
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val year: Int?,
    val songCount: Int,
)

/** An artist name with its track count. Songs are matched by artist name. */
data class Artist(
    val name: String,
    val songCount: Int,
)

/** A folder (relative path) that contains music, with its track count. */
data class Folder(
    val path: String,
    val displayName: String,
    val songCount: Int,
)
