# 06 - Phase 2 Completion and Phase 3 - Library

Status: **implemented, code-reviewed, and device-verified on the Pixel 7a.**

## Phase 2 completion (playback wired into the UI)

- `playback/PlaybackViewModel.kt` — Activity-scoped owner of `PlayerConnection`; exposes `StateFlow<PlaybackUiState>`, delegates play/pause/seek/next/prev/shuffle/repeat and `setPositionUpdatesEnabled`. Closes the connection on `onCleared` (playback lives in the service, so this never stops audio).
- `ui/MiniPlayer.kt` — bar above the bottom nav; shows artwork/title/artist + play-pause; tap opens Now Playing; visible only while connected with a current song.
- `ui/nowplaying/NowPlayingScreen.kt` — full player: large artwork (max 420dp), title/artist, thin seek bar with time labels, control row (shuffle / previous / big play-pause / next / repeat cycle off->all->one). Enables 500ms position updates only while visible (DisposableEffect).
- `ui/PlayerApp.kt` — now wires `LibraryViewModel` + `PlaybackViewModel`; mini player + nav bar in the bottom bar; bottom bar hidden on full-screen routes.

### Fixed while wiring

1. **Slider seek-loop**: Material3 `Slider` can report *programmatic* value changes (from 500ms position updates) as user interactions, causing repeated seeks + codec reclaim + position jumps. Fixed by only treating `onValueChange` values that differ from the published position as drags; slider disabled when duration is unknown. Verified: positions now advance linearly (2.9 -> 5.9 -> 8.9s).
2. **Queue rebuild churn**: `PlayerConnection.publishState` now caches the queue and rebuilds it only on `EVENT_TIMELINE_CHANGED` / `EVENT_MEDIA_ITEM_TRANSITION` (note: `EVENT_MEDIA_ITEMS_CHANGED` does NOT exist in Media3 — it is `EVENT_TIMELINE_CHANGED`).

## Phase 3 — MediaStore library

### Repository (`data/media/MediaStoreRepository.kt`)

- `querySongs()` — all playable tracks (IS_MUSIC), projection per spec.
- `queryAlbums()` / `queryArtists()` — grouped from the catalog with counts (works on all API levels; avoids API-gated columns like NUMBER_OF_SONGS).
- `queryFolders()` — grouped by MediaStore `RELATIVE_PATH`.
- `querySongsInFolder(path)` — the only live detail query (album/artist tracks derive from the in-memory catalog; per-album/artist MediaStore queries were removed as dead code in review).
- Maps MediaStore's literal `<unknown>` to "Unknown artist"/"Unknown album" via `toDisplayString`.

### Models (`model/Library.kt`)

- `Album(id, name, artist, year, songCount)`, `Artist(name, songCount)`, `Folder(path, displayName, songCount)`. `Song` unchanged.

### ViewModel (`ui/library/LibraryViewModel.kt`)

- Loads the catalog once; exposes `songs` (re-sorted live per settings), `albums`, `artists`, `folders`, plus `songsForArtist()` (in-memory) and `loadFolderSongs()` (MediaStore query — folder membership isn't in the Song model).

### Screens

- `ui/songs/SongsScreen.kt` — LazyColumn, artwork + title + artist + duration, tap plays the whole list from that index.
- `ui/albums/AlbumsScreen.kt` — `LazyVerticalGrid` (Adaptive 140dp) of covers.
- `ui/albums/AlbumDetailScreen.kt` — cover, album/artist/count, tracks ordered by track number ("·" when untagged); tap plays the album queue.
- `ui/artists/ArtistsScreen.kt` — list; tap plays the artist's whole discography (no artist detail screen yet).
- `ui/folders/FoldersScreen.kt` + `ui/folders/FolderDetailScreen.kt` — folder browser; tap plays folder queue.
- Navigation: `album/{albumId}` (Long arg) and `folder/{folderPath}` (String, `Uri.encode`d).

## Device verification (Pixel 7a, real library)

| Check | Result |
|---|---|
| Songs | 116 songs with durations |
| Albums | 5 albums; detail shows 42-song track list; tap -> PLAYING (item 0) |
| Artists | 1 artist (all tracks untagged), 129 songs |
| Folders | 3 folders (download 42, WhatsApp Audio 72, WhatsApp Documents 2); detail -> PLAYING |
| Unknown metadata | "Unknown artist" (no more `<unknown>`) |
| Now Playing seek | advances linearly, no seek loop |
| Folder with space in path | "WhatsApp Audio" opens via `Uri.encode` route, 72 songs, no crash |

## Code review fixes applied

- Removed dead `querySongsForAlbum()` / `querySongsForArtist()` from `MediaStoreRepository` — album/artist data is derived from the in-memory catalog (`LibraryViewModel.songs`), only `querySongsInFolder()` is a live query.
- Folder query uses `RELATIVE_PATH LIKE "$path/%"` instead of `"$path%"` (the `%` prefix would also match sibling folders like `downloads/…`).
- Unknown albums (`ALBUM_ID` 0 or -1) are merged into a single "Unknown album" group via `albumId.coerceAtLeast(0)`.
- Import/comment cleanups (`TextAlign` import, unused `widthIn`/`PaddingValues` imports, cache-comment correction).

## Known gaps / next

- Search, artist detail screen (currently tap = play all), sort UI, folder exclusion, queue screen, hardcoded strings -> strings.xml.
- UI AI coordination: the other AI may restyle files under `ui/` but must keep the composable signatures listed in the root README of this folder (stable contracts).
