# Optimized Music Downloader Module — AI Coder Instructions

## Goal

Add an optional, audio-focused downloader module to the existing native Android music player without hurting playback performance, battery life, startup time, or background efficiency.

Take inspiration from YTDLnis / yt-dlp architecture, but keep this implementation stripped down for music. Do not merge or copy an entire downloader app.

## Core Rules

- Keep downloader code isolated from the player core.
- Downloader must do zero background work when idle.
- No permanent downloader service.
- No constant polling.
- No ads, analytics, cloud sync, or accounts.
- Do not initialize downloader runtimes during normal app startup.
- Downloader failures must never break local playback.
- Use only for content the user is authorized to download and sources that permit it.
- Do not implement DRM bypass or protected-stream circumvention.

## Architecture

```text
Player Core
  ├── Media3 / ExoPlayer
  ├── MediaStore
  └── Player UI

Optional Downloader
  ├── URL input
  ├── Metadata extraction
  ├── Audio format selection
  ├── Download queue
  ├── Download worker
  └── MediaStore update
```

The downloader should initialize only when the user opens Downloads or starts a download.

## Downloader Approach

Prefer a yt-dlp-backed design for broad source support.

Reuse only the ideas needed for:
- URL validation
- metadata extraction
- audio format discovery
- best-audio selection
- retries
- cookies when later required
- progress reporting
- safe filenames
- post-processing when necessary

If a source exposes a direct audio URL, prefer the simpler direct-download path.

## Download Flow

```text
Paste/share URL
    ↓
Validate URL
    ↓
Fetch metadata
    ↓
Show title, thumbnail, artist/uploader, duration,
available audio formats, estimated size
    ↓
Choose quality/format
    ↓
Queue download
    ↓
Download
    ↓
Optional light post-processing
    ↓
Save to Music folder
    ↓
Update MediaStore
    ↓
Track appears in player
```

## Downloads UI

Create one Downloads screen with:
- Active
- Queued
- Completed
- Failed

Each item should show:
- title
- thumbnail
- source
- progress
- downloaded size / total size
- speed
- status
- cancel
- retry
- pause/resume only when genuinely supported

Settings:
- audio quality
- output format
- destination folder
- Wi-Fi only
- charging only
- max concurrent downloads

Keep the UI visually consistent with the minimalist player.

## Audio Defaults

Default to music, not video.

Preferred:
- best available audio
- original audio container when practical
- M4A/AAC
- Opus
- MP3 only when conversion is explicitly requested

Avoid transcoding if the source already provides a usable audio format.

Transcoding costs CPU, battery, time, and can reduce quality.

## Post-processing

Only when needed:
- metadata tagging
- thumbnail embedding
- filename cleanup
- container remux
- optional conversion

If FFmpeg is required, use it only for download work and release it afterward.

## File Naming

Default:

```text
Artist - Title.ext
```

Fallback:

```text
Uploader - Title.ext
```

Final fallback:

```text
Title.ext
```

Sanitize invalid characters, excessive length, and path-like input.

Handle duplicates with skip / overwrite / rename behavior. Do not silently create endless duplicates.

## Storage

Default destination:

```text
Music/<AppName>/Downloads/
```

Prefer scoped storage and MediaStore-compatible APIs.

Do not request broad filesystem access unless absolutely necessary.

After completion:
- insert/update MediaStore correctly
- make the song available immediately
- avoid a full library rescan when a targeted update is enough

## Queue and Concurrency

Default simultaneous downloads:

```text
1
```

Optional maximum:

```text
2
```

Avoid high parallelism because it increases battery use, heat, storage contention, and network load.

## Background Execution

During an active download:
- use the correct Android foreground/background execution model
- show a proper notification
- expose progress
- support cancellation

When no downloads exist:

```text
no service
no worker
no timer
no polling
```

## Network Controls

Support:
- Wi-Fi only
- any network
- optional charging-only mode

Use Android network constraints/APIs instead of custom connectivity polling.

## Retry Policy

Retry only transient failures:
- temporary network loss
- timeout
- server 5xx
- interrupted connection

Do not aggressively retry:
- invalid URL
- unsupported source
- authorization failure
- removed content
- DRM-protected content
- permanent HTTP errors

Use bounded exponential backoff.

## Resume

Support resume only when the source/protocol actually supports it.

If partial files cannot be resumed, clean them safely after failure/cancel when appropriate.

## Metadata

Where available:

```text
title → track title
artist/uploader → artist
album/playlist → collection
thumbnail → artwork
date → optional metadata
source URL → optional internal metadata
```

Do not replace good embedded metadata with worse guessed metadata.

## Security

Treat remote data as untrusted.

Sanitize:
- filenames
- paths
- metadata strings
- command arguments

Never build shell commands by concatenating raw user input.

Do not expose arbitrary command execution in the normal UI.

If expert yt-dlp arguments are added later, isolate them behind an explicit advanced mode.

## Performance Requirements

Downloader must not degrade playback.

During playback + download:
- no audio stutter
- low CPU overhead
- no excessive parallel I/O
- no huge in-memory buffers
- stream data to disk
- throttle nonessential UI updates

Progress UI only needs roughly:

```text
500–1000 ms updates
```

## Battery Requirements

Default strategy:
- one download at a time
- no transcoding unless needed
- no repeated metadata passes
- no constant network polling
- no background work after queue finishes
- no full MediaStore rescans
- no repeated thumbnail downloads

## Startup Requirement

Do not initialize yt-dlp, Python, FFmpeg, or any heavy downloader runtime in `Application.onCreate()`.

Lazy-load downloader components only when needed.

Normal player startup must remain essentially unchanged.

## Failure Isolation

If downloader initialization fails:
- player still launches
- local playback still works
- library browsing still works
- downloader shows its own error state

## Suggested Package Structure

```text
downloader/
  DownloaderRepository.kt
  DownloadManager.kt
  DownloadWorker.kt
  DownloadState.kt
  DownloadRequest.kt
  DownloadResult.kt
  MetadataExtractor.kt
  FormatSelector.kt
  FileNaming.kt
  MediaStoreWriter.kt
  ui/
    DownloadsScreen.kt
    DownloadDetailsScreen.kt
```

Hide yt-dlp-specific code behind an interface:

```kotlin
interface MediaExtractor {
    suspend fun getMetadata(url: String): MediaMetadata
    suspend fun getFormats(url: String): List<AudioFormat>
}
```

## V1 Features

Implement:
- paste URL
- Android share-to-app
- metadata preview
- best-audio download
- selectable audio quality
- selectable format when available
- download queue
- one active download by default
- progress
- cancel
- retry
- completed history
- Wi-Fi only
- destination folder setting
- metadata tagging
- artwork embedding when practical
- MediaStore integration

## Later Features

Only after V1 is stable:
- playlist/batch downloading
- cookies/imported sessions
- authentication helpers
- lyrics/subtitle extraction
- archive/history file
- advanced format rules
- custom yt-dlp arguments
- more concurrent downloads
- scheduled downloads

## Do Not Implement Initially

- video editor
- video cropping
- video-resolution-heavy UI
- social-media browser
- built-in web browser
- cloud downloader
- automatic scraping loops
- background URL monitoring
- bulk parallel downloads
- always-running Python runtime

## Test Cases

Test:
- valid direct audio URL
- supported yt-dlp URL
- unsupported URL
- invalid URL
- network loss
- cancel midway
- retry
- duplicate filename
- missing metadata
- missing thumbnail
- huge file
- app backgrounded
- screen off
- playback while downloading
- Bluetooth playback while downloading
- queue completion
- process recreation
- storage full
- storage/permission failure

## Acceptance Criteria

The downloader is ready when:

1. Normal music-player startup is not measurably worsened.
2. No downloader process/service remains active after downloads finish.
3. Active downloads do not cause audio stutter.
4. Completed songs appear in MediaStore/player automatically.
5. A failed download cannot crash the player.
6. Idle battery usage is effectively unchanged.
7. The module remains audio-focused and avoids unnecessary video/downloader complexity.
