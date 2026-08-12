# 03 - Phase 2 - Playback Engine

Status: **backend complete; not wired to the UI as of this file's creation.**

## Files

- `playback/PlaybackService.kt` — `MediaSessionService` owning one `ExoPlayer` + one `MediaSession`.
  - Audio attributes (USAGE_MEDIA / music), `setHandleAudioBecomingNoisy(true)`.
  - Audio offload preference enabled via `TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED` (preference, not requirement — unsupported routes fall back).
  - `@UnstableApi` kept on the class (verified the new offload API is still unstable in 1.9.3).
  - No custom wake-lock; if offload cannot engage for a track, screen-off playback should be verified on device.
- `playback/PlayerConnection.kt` — the UI's only route to the service. Owns a `MediaController` (never an `ExoPlayer`).
  - Exposes `StateFlow<PlaybackUiState>`; publishes at most every 500 ms and only while playing + enabled (`setPositionUpdatesEnabled`).
  - Queue ops: `playQueue`, `setQueue`, `addNext`, `addToQueueEnd`, `removeQueueItem`, `moveQueueItem`, `clearQueue`; plus play/pause/seek/next/previous/shuffle/repeat.
  - `Song` <-> `MediaItem` round-trip via media metadata extras.
- `playback/PlaybackUiState.kt` — immutable snapshot (connected, current song, isPlaying, repeat, shuffle, duration, position, queue, index) + `PlaybackRepeatMode` mapping.

## Known issues (as of creation)

- `publishState` rebuilt the entire queue list on every 500 ms tick (allocation churn for large queues). Fix plan: cache the queue and rebuild only on media-item change events.
- `PlayerConnection` was never instantiated — the engine was dead code until Phase 3 wiring.
