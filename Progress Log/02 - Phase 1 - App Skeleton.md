# 02 - Phase 1 - App Skeleton (Done)

## Entry points & DI

- `App.kt` — `PlayerApplication` + `AppContainer` (manual DI; holds `SettingsRepository`, `MediaStoreRepository`).
- `MainActivity.kt` — edge-to-edge, audio permission flow:
  - `READ_MEDIA_AUDIO` on API 33+; `READ_EXTERNAL_STORAGE` below.
  - **Known gap:** permission is only checked in `onCreate`; revoking it in Settings while the app is backgrounded leaves stale UI until recreation (add an `onResume` re-check).

## Theme

- `ui/PlayerTheme.kt` + `ui/PlayerThemeValues.kt` — monochrome palette (black/white/gray, `#111111`/`#FEFEFE`), AMOLED-friendly dark scheme, custom Typography.
- `SettingsRepository` persists `ThemeMode` (SYSTEM/LIGHT/DARK), dynamic color, song sort, restore-on-launch via DataStore.

## Navigation shell

- `ui/PlayerApp.kt` — 4 library destinations (Songs, Albums, Artists, Folders), bottom `NavigationBar`, `NavHost`.
- All four tabs render a shared `LibraryEmptyState` placeholder ("No X yet").
- `AudioPermissionRequired` screen shown when permission is missing.

## Manifest

- Permissions: `READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE (maxSdk 32)`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`.
- `PlaybackService` declared with `foregroundServiceType="mediaPlayback"` + Media3 `MediaSessionService` intent filter.
- `POST_NOTIFICATIONS` not declared (media-session notifications are exempt; only needed if the notification must persist while paused).
