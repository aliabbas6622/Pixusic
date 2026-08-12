# Architecture and Data

## Playback ownership

The `Activity` must **not own ExoPlayer**.

Correct architecture:

```text
MainActivity / Compose UI
        │
        ▼
MediaController / PlayerConnection
        │
        ▼
MediaSessionService
        │
        ▼
MediaSession
        │
        ▼
ExoPlayer
        │
        ▼
Android audio stack
```

This allows playback to survive Activity recreation and supports background playback, notification controls, Bluetooth/headset controls, lock-screen controls, and audio focus correctly.

## PlaybackService responsibilities

`PlaybackService` should:

- create one ExoPlayer instance
- create one MediaSession
- expose playback through Media3
- restore last queue if configured
- release player/session when service is destroyed
- let Media3 handle media notification integration
- avoid custom wake-lock logic unless platform testing proves it necessary

## UI connection

Create a small `PlayerConnection` abstraction around `MediaController`.

It should expose only UI-relevant state such as:

```kotlin
data class PlaybackUiState(
    val currentSong: Song?,
    val isPlaying: Boolean,
    val repeatMode: RepeatMode,
    val shuffleEnabled: Boolean,
    val durationMs: Long,
    val positionMs: Long,
    val queueSize: Int
)
```

Do not expose ExoPlayer directly to every composable.

## Local music source

Use `MediaStore.Audio.Media`.

Recommended projection fields:

- `_ID`
- `TITLE`
- `ARTIST`
- `ARTIST_ID` when useful
- `ALBUM`
- `ALBUM_ID`
- `DURATION`
- `TRACK`
- `YEAR`
- `MIME_TYPE`
- `DATE_MODIFIED`
- `RELATIVE_PATH` where available

Create content URIs using MediaStore IDs. Avoid converting everything into raw filesystem paths.

## Song model

Keep it lightweight:

```kotlin
data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val mimeType: String?,
    val dateModified: Long
)
```

Do not put these inside every `Song`:

- Bitmap
- byte arrays
- lyrics blobs
- player references
- open streams

## MediaStore vs Room

**MediaStore is the music catalog.**

Do not duplicate every discovered audio item into Room.

Use Room only for app-owned state, for example:

```text
favorites
playlists
playlist_entries
play_history
bookmarks
user_tags
folder_exclusions
metadata_overrides
```

Suggested minimal tables:

```text
Favorite(songId PK)

Playlist(
  id PK,
  name,
  createdAt
)

PlaylistEntry(
  playlistId,
  songId,
  sortIndex,
  PK(playlistId, songId)
)

PlayHistory(
  id PK,
  songId,
  playedAt,
  durationPlayedMs
)
```

## Library refresh

Do not continuously scan storage.

Use:

- initial MediaStore query
- re-query when app resumes if needed
- ContentObserver or version/change checks if useful
- explicit refresh action

Avoid recurring WorkManager jobs for library scanning.

## State ownership

Recommended:

```text
Compose screen
    ↓
ViewModel
    ↓
Repository / PlayerConnection
    ↓
MediaStore / Room / MediaController
```

Use `StateFlow` for screen state.

Avoid:

- global event bus
- giant Redux store
- passing mutable collections through the UI
