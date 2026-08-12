# 10 - Real Queue Management and Album Cleanup

Status: **Implemented, verified, and compiling cleanly (BUILD SUCCESSFUL).**

## Issues Fixed

### 1. Queue — Now Does Real Work
The old queue screen only showed the current playback queue with no way to add songs. **Completely rebuilt** `QueueScreen.kt` with two tabs:

**Tab 0 — "Now Playing"**
- Current playback queue list with active-track highlighted row (animated background color).
- Auto-scrolls to the currently playing song when you open the screen.
- Tap any row to jump to that song immediately.
- Remove individual items with the `✕` button.
- Clear entire queue via the trash icon in the top bar.

**Tab 1 — "Add Songs"**
- Browses ALL library songs with instant search.
- Each row shows two quick-action buttons:
  - `▶+` — **Play Next**: inserts the song immediately after the current track.
  - `⊕` — **Add to Queue**: appends the song to the end of the queue.
- Songs already in the queue are dimmed (still addable again).
- Users can tap multiple songs in order to build up a custom queue.

### 2. Songs Screen — Overflow Menu Per Song
Added a `⋮` (MoreVert) overflow menu to every `SongRow` in `SongsScreen.kt`:
- **Play** (default tap on song title/artwork area — unchanged).
- **Play Next** — inserts song after current track via `playbackViewModel.addNext(song)`.
- **Add to Queue** — appends to end via `playbackViewModel.addToQueueEnd(song)`.

### 3. Album / Songs Junk Cleanup (WhatsApp Audio, Documents, etc.)
Moved all filtering logic from UI layer to **MediaStore query level** in `MediaStoreRepository.kt`:
- Added `DURATION >= 10_000ms` constraint on all queries — removes short notification sounds, WhatsApp voice notes (typically < 5s), etc.
- Added `EXCLUDED_KEYWORDS` list: `whatsapp`, `voice note`, `voicenote`, `call_rec`, `callrecord`, `recordings`, `notification`, `ringtone`, `alarm`.
- Applied keyword exclusion on `RELATIVE_PATH`, album name, and song title — so WhatsApp Audio, WhatsApp Documents, and WhatsApp Voice Notes folders all disappear from Songs, Albums, Artists, and Folders tabs.
- Removed the old in-memory `isWhatsAppVoiceNote` filter from `LibraryViewModel` (now always filtered at source).
- Removed the old filter toggle button from `SongsScreen.kt` header.

## New Delegate Methods Added
- `PlaybackViewModel.addNext(song: Song)` → `connection.addNext(song)`
- `PlaybackViewModel.addToQueueEnd(song: Song)` → `connection.addToQueueEnd(song)`
- `PlaybackViewModel.moveQueueItem(fromIndex, toIndex)` → `connection.moveQueueItem(...)`
