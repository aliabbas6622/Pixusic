# 05 - Code Review Findings (initial review)

## Strengths

- Correct architecture: player lives in a `MediaSessionService`; UI only talks through `PlayerConnection`; immutable `PlaybackUiState`.
- Battery discipline: gated 500 ms position polling, audio-offload preference, no wakelock hacks, no scans.
- Lightweight `Song` model (no bitmaps/streams); minimal dependency set; R8 enabled for release.

## Issues (ranked)

1. **Playback engine not wired to the UI** — `PlayerConnection` is never instantiated; app cannot play anything. (Being fixed in the next phase.)
2. **`publishState` queue rebuild every 500 ms** — allocates the full queue list twice a second. (Fix: cache queue, rebuild on media-item changes — applied during UI wiring.)
3. **No wake mode** — relies on audio offload for screen-off playback; verify with a non-offloadable track (e.g., FLAC) on device.
4. **No Gradle wrapper / AGP 9.3.1 needed Gradle 9.5+** — fixed; see `01 - Project Setup and Build Tooling.md`.
5. **Permission re-check gap** in `MainActivity` (no `onResume` re-check).
6. Minors: hardcoded UI strings (not in `strings.xml`), non-adaptive launcher icon, `POST_NOTIFICATIONS` not declared, empty `icon = {}` in nav items (fixed when icons were added).

## Status of fixes

| Issue | Status |
|---|---|
| #1 Playback not wired | Fixed in `06 - Phase 2 Completion and Phase 3 - Library.md` |
| #2 Queue rebuild churn | Fixed in `PlayerConnection.publishState` (cached queue) |
| #4 Gradle wrapper | Fixed (see `01`) |
