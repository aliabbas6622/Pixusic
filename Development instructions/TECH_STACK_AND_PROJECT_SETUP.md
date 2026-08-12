# Tech Stack and Project Setup

## Required stack

| Area | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Playback | AndroidX Media3 / ExoPlayer |
| Media session | Media3 `MediaSession` + `MediaSessionService` |
| Local library | Android `MediaStore` |
| Async | Kotlin Coroutines + Flow / StateFlow |
| Preferences | DataStore |
| App-owned relational data | Room, only where useful |
| Artwork | Coil Compose |
| Navigation | Navigation Compose |
| Performance | Macrobenchmark, Baseline Profiles, Startup Profiles, Perfetto |

Use the **latest stable versions compatible with API 37** available through Android Studio/Google repositories. Do not pin random beta versions unless API 37 requires them.

## Suggested dependencies

Use only what is needed:

```kotlin
implementation(platform(...composeBom...))
implementation(...activity-compose...)
implementation(...material3...)
implementation(...navigation-compose...)
implementation(...lifecycle-runtime-compose...)
implementation(...lifecycle-viewmodel-compose...)

implementation(...media3-exoplayer...)
implementation(...media3-session...)

implementation(...datastore-preferences...)

implementation(...coil-compose...)
```

Add Room only when favorites/playlists/history persistence is implemented.

## Manifest essentials

The project will need at least:

```xml
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

Service shape:

```xml
<service
    android:name=".playback.PlaybackService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback">

    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

Verify the final manifest against current Android 17 + Media3 official documentation while implementing.

## Project structure

Keep this a small app. Do not create dozens of Gradle modules.

Recommended initial structure:

```text
app/
  src/main/java/<package>/
    App.kt

    playback/
      PlaybackService.kt
      PlayerConnection.kt
      PlaybackState.kt

    data/
      media/
        MediaStoreRepository.kt
      database/
        AppDatabase.kt
        dao/
        entity/
      settings/
        SettingsRepository.kt

    model/
      Song.kt
      Album.kt
      Artist.kt
      Playlist.kt

    ui/
      navigation/
      library/
      songs/
      albums/
      artists/
      folders/
      search/
      nowplaying/
      queue/
      playlists/
      settings/

    artwork/
      ArtworkRepository.kt
```

Optional later modules:

```text
:app
:benchmark
:baselineprofile
```

## Dependency injection

Start with manual wiring:

```text
AppContainer
  ├── MediaStoreRepository
  ├── SettingsRepository
  ├── PlaylistRepository
  └── ArtworkRepository
```

Do not add Hilt unless the project genuinely becomes complex enough to justify it.

## Network policy

V1 should not require internet access.

Do not add:

- Retrofit
- OkHttp
- Firebase
- analytics SDKs
- cloud services

If a future feature needs networking, add it in isolation rather than making networking part of the core app.
