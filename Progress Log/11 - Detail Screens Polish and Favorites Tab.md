# 11 - Detail Screens Polish + Favorites Tab

Status: **Implemented, verified, and compiling cleanly (BUILD SUCCESSFUL).**

## Features Added

### 1. Favorites Tab (5th navigation bar tab)
- New `FavoritesScreen.kt` — shows all songs the user has hearted.
- **Empty state** — large heart icon + "Tap the heart icon on any song" message.
- **Play All** and **Shuffle All** action buttons when favorites exist.
- Per-row `⋮` overflow menu with **Play Next** and **Add to Queue**.
- Tapping a song plays the full favorites list from that position.
- Added `LibraryDestination.Favorites` with heart outline icon to bottom nav.

### 2. Album Detail — Action Buttons + Per-Track Overflow
- `AlbumDetailScreen.kt` rebuilt with:
  - **Play All** (filled black button) — starts album from track 1.
  - **Shuffle** (outlined button) — starts album in random order.
  - Per-track `⋮` overflow → **Play Next** / **Add to Queue**.
  - Slightly refined artwork shadow and sizing (240dp max, 20dp radius).

### 3. Artist Detail — Centered Header + Action Buttons + Per-Track Overflow
- `ArtistDetailScreen.kt` rebuilt with:
  - **Centered avatar** (88dp circle with person icon) above artist name.
  - **Play All** and **Shuffle** action buttons.
  - Per-track `⋮` overflow → **Play Next** / **Add to Queue**.

### 4. `PlayerApp.kt` Changes
- `LibraryDestination.Favorites` added to the enum (5th tab).
- `FavoritesScreen` wired with all callbacks from `LibraryViewModel` + `PlaybackViewModel`.
- `AlbumDetailScreen` and `ArtistDetailScreen` call sites updated with `onPlayAll`, `onShuffleAll`, `onAddNext`, `onAddToQueueEnd`.
