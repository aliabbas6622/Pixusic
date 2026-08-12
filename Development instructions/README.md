# Pixel 7a Music Player — AI Coder Master Brief

## Objective

Build a **native Android offline music player** for a **Google Pixel 7a running Android 17 / API 37**.

Primary priorities, in order:

1. Lowest practical battery use during long screen-off playback.
2. Minimal CPU wakeups and background work.
3. Low memory use.
4. Fast startup and smooth library scrolling.
5. Correct Android media/background behavior.
6. Clean minimalist UI based on the approved Now Playing concept.
7. Maintainable Kotlin code with as few dependencies as practical.

## Hard constraints

- Language: **Kotlin**
- IDE/toolchain: **Android Studio only**
- UI: **Jetpack Compose**
- Playback: **AndroidX Media3 / ExoPlayer**
- Local music source: **MediaStore**
- No Flutter, React Native, Node.js, Python runtime, Electron, KMP, or extra large framework toolchains.
- No ads.
- No analytics.
- No cloud sync.
- No account system.
- No network requirement for V1.
- Do not build a custom audio decoder/engine.
- Do not add FFmpeg unless a real unsupported-format requirement appears.
- Do not add Hilt initially; use simple manual dependency wiring.
- Prefer platform/Jetpack APIs over third-party libraries.

## SDK direction

- `compileSdk = 37`
- `targetSdk = 37`
- `minSdk = 29`

If the local Android Studio installation uses a slightly different API 37 setup, adapt only what is required to compile, while preserving Android 17 behavior.

## Product identity

This app should feel like:

- Gramophone simplicity
- Musicolet-style strong queue handling
- Symphony-style lightweight library browsing
- Oto-style polish
- selected advanced audio options later

The app should **not** try to beat Poweramp by feature count. It should win on simplicity, speed, battery use, clean offline behavior, and Pixel-friendly design.

## V1 definition

V1 must provide:

- Local music discovery
- Songs
- Albums
- Artists
- Folders
- Search
- Sort
- Play / pause
- Previous / next
- Seek
- Shuffle
- Repeat off / one / all
- Queue
- Queue reorder/remove
- Favorites
- User playlists
- Mini player
- Full Now Playing screen
- Media notification
- Lock-screen controls
- Bluetooth/headset controls
- Background playback
- Audio focus handling
- Resume last queue/song
- Light / dark / system theme

## Non-goals for V1

Do not implement these until core performance is measured:

- Live visualizer
- animated waveform
- animated album-art background
- cloud services
- online streaming
- lyrics scraping
- AI features
- social features
- automatic web metadata lookup
- Chromecast
- Android Auto
- advanced EQ
- crossfade
- pitch shifting
- heavy DSP

## AI coder rules

1. Read all 7 markdown files before writing production code.
2. Do not redesign the architecture without a concrete Android limitation.
3. Keep dependencies minimal.
4. Build and fix compiler errors after each phase.
5. Avoid placeholder implementations in completed phases.
6. Avoid continuous polling or background jobs when event-driven APIs exist.
7. Keep the player in a `MediaSessionService`, not in the Activity.
8. Treat battery/performance requirements as functional requirements.
9. Prefer immutable UI models and small Compose recomposition scopes.
10. Do not mirror the entire MediaStore library into Room.

## Final quality bar

The app is successful when it can play local music for long periods with the screen off while the UI process does almost no unnecessary work, and the user can browse a large library smoothly without obvious jank or excessive memory use.
