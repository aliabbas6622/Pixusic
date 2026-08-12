# 09 - Now Playing Vertical Balance, Queue Wiring, and GitHub

Status: **Implemented, compiled (BUILD SUCCESSFUL), device-verified on the Pixel 7a, and pushed to GitHub.**

## 1. Now Playing vertical balance fix (user-reported)

**Problem:** On the tall Pixel 7a screen (~914dp) the Now Playing content was top-anchored in a
scrollable column. The content filled only ~790dp, leaving ~120dp of dead space at the bottom —
so the seek bar, controls pill, and utility row all sat too high on the Y axis.

**Fix — `ui/nowplaying/NowPlayingScreen.kt`:**
1. Outer `Box` now applies `windowInsetsPadding(WindowInsets.safeDrawing)` so the screen no longer
   draws under the status bar / gesture area (the app renders edge-to-edge; the full-screen routes
   ignore the Scaffold's `innerPadding`).
2. The content was restructured into an outer `Column` holding the fixed top bar, then a
   `Box(Modifier.weight(1f), contentAlignment = Alignment.Center)` wrapping the scrollable body
   column. When content is shorter than the viewport it is vertically centered (controls sit in
   the lower third); when it overflows (small screens / large fonts) the inner column scrolls
   normally — the `verticalScroll` + parent-centering combo avoids the "unreachable top" footgun
   of `Arrangement.Center` inside a scrollable.
3. The scrollable column lost its `fillMaxSize()` (would defeat centering) and keeps the 24dp
   horizontal padding.

**Verified:** user confirmed on-device that the position now looks right ("position is ok").

## 2. Queue feature completion (build was red, now green)

The UI AI's `08` pass added `ui/queue/QueueScreen.kt`, the `queue` route, and the `onOpenQueue`
hook, but the project did **not** compile: `PlaybackViewModel` was missing the three delegates the
`QueueScreen` wiring calls.

**Fixed — `playback/PlaybackViewModel.kt`:** added
`seekToQueueItem(index, positionMs = 0L)`, `removeQueueItem(index)`, `clearQueue()` (one-line
delegates to `PlayerConnection`, which already had the Media3 controller ops). Build verified
`BUILD SUCCESSFUL`. Also cleaned up duplicate overloads that appeared while the UI AI edited the
same file in parallel.

## 3. Pushed to GitHub

- Repo: **https://github.com/aliabbas6622/Pixusic** (public, empty at first push, default branch `main`).
- `git init -b main`, `.gitignore` already correct (excludes `.gradle/`, `build/`, `local.properties`, `.idea/`, `*.log`).
- Identity: `aliabbas6622 <aliabbas6622@users.noreply.github.com>` (GitHub noreply).
- Auth: Git Credential Manager (system `credential.helper = manager`) handles HTTPS silently.
- Commits:
  - `6d59409` — full project baseline (58 files, +5122): phases 1–3 + UI polish, docs, screenshots.
  - `ee2638c` — "Add Now Playing vertical centering, queue screen, artist detail, and song search"
    (13 files, +717/−49): the `09` fix, `QueueScreen`, `ArtistDetailScreen`, Songs search bar,
    `Progress Log/08`, and `screenshot_nowplaying_centered.png`.
- Latest build installed on the Pixel 7a matches the pushed `main`.

## Known gaps / next

- **Room persistence (Phase 4)** — Favorites / Playlists / Play History.
- **Strings migration** — hardcoded UI strings → `strings.xml`.
- **Sort UI** — user-facing sort options (Title / Artist / Album / Date Added).
- **Search** currently filters the Songs catalog only; extend to Albums / Artists / Folders.
- **Code review items** — wake-mode check (#3), permission re-check on resume (#5).
- **Performance (Phase 6/7)** — baseline/startup profiles not started.
