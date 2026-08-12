# 13 - Sleep Timer, Audio Diagnostics, and Lyrics Engine

Status: **Implemented, verified, and compiling cleanly (BUILD SUCCESSFUL).**

## Features Implemented

### 1. Sleep Timer (10m / 20m / 30m / 60m / End of Track + Fade-Out)
- Added `sleepTimerRemainingMs: StateFlow<Long?>` and coroutine-based timer logic to `PlaybackViewModel.kt`.
- Options:
  - **10m, 20m, 30m, 60m**: Starts countdown, updates remaining time live in UI.
  - **End of Track**: Automatically pauses playback as soon as current song finishes.
  - **Smooth Fade-Out**: Over the final 10 seconds of the timer, audio volume gradually ramps down to 0 before pausing, then restores normal volume.
  - **Live Indicator**: The "More" button on `NowPlayingScreen` displays `More (⏱)` when a sleep timer is active.

### 2. Audio Diagnostics Sheet
- Accessible from `NowPlayingScreen` ➔ **More** ➔ **Audio Diagnostics**.
- Displays real-time details for the active audio stream:
  - **Format / MIME Type**: (e.g. `audio/mpeg`, `audio/flac`, `audio/aac`).
  - **Duration & Exact File URI / Path**.
  - **Audio Offload Status**: Confirms ExoPlayer low-power hardware DSP offload mode.
  - **Output Device Route**: System active routing.

### 3. Synchronized Lyrics Engine & LRC Support
- Created `LrcParser.kt` in `data/lyrics/`.
- Parses standard `.lrc` timestamped files (`[mm:ss.xx] Lyric line`).
- Automatically checks for matching `.lrc` files in the audio file's folder.
- Interactive `Lyrics` bottom sheet on `NowPlayingScreen` displays synced lyric lines.
