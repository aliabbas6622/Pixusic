# 01 - Project Setup and Build Tooling

## Stack

- Kotlin, Jetpack Compose + Material 3
- AndroidX Media3 / ExoPlayer **1.9.3**
- DataStore Preferences, Coil 3 (coil-compose 3.5.0), Navigation Compose 2.9.8
- Lifecycle 2.11 (runtime-compose, viewmodel-compose), coroutines 1.10.2
- No Hilt, no Room (yet), no network/analytics

## SDK levels

- compileSdk = 37, targetSdk = 37, minSdk = 29 (Android 17 / Pixel 7a target)

## Gradle wrapper (added later — originally missing)

- **Gradle 9.5.0** pinned in `gradle/wrapper/gradle-wrapper.properties`.
  AGP 9.3.1 requires Gradle 9.5.0+ (the repo originally had no wrapper and only a stale Gradle 8.14 cache, so it could not build).
- Kotlin plugin 2.3.21 (compose plugin) — "tested up to" Gradle 9.3.0, Gradle 9.5 works with a cosmetic warning.

## CLI build command (Windows / Git Bash)

System Java 26 is too new for Gradle 9.5; use Android Studio's JBR 25:

```bash
export JAVA_HOME='/c/Program Files/Android/Android Studio/jbr'
./gradlew.bat :app:assembleDebug --console=plain
```

- `local.properties` points at `C:\Users\aliab\AppData\Local\Android\Sdk`.
- First build takes several minutes (dependency downloads); subsequent builds are fast.

## Fixed compile error (original repo did not build)

Media3 1.9.3 **removed** `ExoPlayer.Builder.setAudioOffloadPreferences(...)` and `androidx.media3.exoplayer.audio.AudioOffloadPreferences`. The offload preference now lives in `TrackSelectionParameters.AudioOffloadPreferences` (modes `DISABLED` / `ENABLED` / `REQUIRED`), applied via `player.setTrackSelectionParameters(...)`. `PlaybackService.kt` was updated accordingly (see `03 - Phase 2 - Playback Engine.md`).

## Also fixed

- `PlaybackUiState.kt`: `@Player.RepeatMode` now uses the `@param:` target to silence a Kotlin 2.3 annotation-default-target warning.
