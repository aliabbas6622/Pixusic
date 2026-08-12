# 12 - Custom Playlists, Appearance Theme Toggle, and Queue Restore

Status: **Implemented, verified, and compiling cleanly (BUILD SUCCESSFUL).**

## Features Implemented

### 1. Custom Playlists (Room DB Engine)
- **Room 2.7.2 Integration**: Created `PlaylistEntity`, `PlaylistSongEntity`, and relational count DTO `PlaylistWithSongCount`.
- **Database & DAO**: `PlaylistDao` provides reactive flow `observePlaylistsWithCount()`, `insertPlaylist`, `updatePlaylist`, `deletePlaylist` (with foreign key cascade to songs), and positional ordering.
- **Repository & ViewModel**: `PlaylistRepository` and `PlaylistViewModel` manage async database transactions cleanly with `StateFlow`.
- **PlaylistsScreen**: Material3 UI displaying all user-created playlists, song counts, new playlist `AlertDialog`, and `⋮` overflow menu for Rename and Delete.
- **PlaylistDetailScreen**: Playlist header with album artwork, **Play All**, **Shuffle**, and track list with per-row **Play Next**, **Add to Queue**, and **Remove from Playlist**.
- **Global "Add to Playlist" Sheet**: Tapping `⋮` -> **Add to Playlist** on any track in `SongsScreen` opens a `ModalBottomSheet` allowing instant addition to any existing playlist or creating a new one inline.

### 2. Appearance Theme Mode Toggle (Light / Dark / System)
- Updated `PlayerTheme` composable to accept dynamic `ThemeMode` (`SYSTEM`, `LIGHT`, `DARK`).
- Settings gear icon `⚙` added to `SongsScreen` header top bar.
- Tapping settings opens an Appearance `ModalBottomSheet` with Material3 `FilterChip` selectors that persist choices via DataStore.

### 3. Automatic Queue Preservation & Restore on Cold Launch
- `SettingsRepository` saves queue song IDs, active index, and playback position `positionMs` into DataStore whenever the queue or index changes.
- On cold launch, `PlayerApp` checks `restorePlaybackOnLaunch` setting, resolves song models from catalog, and restores the queue state without auto-starting playback.
