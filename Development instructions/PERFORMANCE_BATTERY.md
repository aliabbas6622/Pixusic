# Performance and Battery Requirements

Performance is a product requirement, not a cleanup task.

## Audio offload

Prefer Media3 audio offload when device/codec/output conditions allow it.

The player should be designed so the default playback path does not unnecessarily disable offload.

Features that may interfere with offload, such as custom DSP, speed changes, crossfade, some equalizer paths, or audio processors, must be optional.

Future diagnostic UI may expose:

```text
Codec
Sample rate
Output device
Audio offload active/inactive
DSP active/inactive
```

## Background behavior

When the screen is off and music is playing:

- UI should not keep polling player position
- no unnecessary timers
- no periodic database writes
- no storage rescans
- no networking
- no animated UI work
- no custom always-on worker

The foreground media service should exist only as required for legitimate playback.

## Position updates

Bad:

```text
poll currentPosition every frame
```

Preferred:

- when Now Playing is visible: update around every `250–500ms`
- when player UI is not visible: stop UI position polling
- update immediately on seek, track change, pause/play, etc.

Do not make high-frequency position state invalidate the whole app UI.

## Compose rules

- small recomposition scopes
- immutable UI models
- stable list keys
- use `remember` where appropriate
- use `derivedStateOf` only where it actually reduces work
- collect flows lifecycle-aware
- do not pass giant mutable objects through the UI
- do not animate values continuously without visible benefit

## Artwork memory

Never decode huge embedded cover art at original size unless exporting/editing it.

Targets:

- song row: ~64–96px displayed size
- album grid: ~150–250px
- Now Playing: roughly 400–700px depending on screen density/layout

Let Coil decode near requested size.

Keep caches bounded.

## Storage/database writes

Do not write current playback position every second.

Persist on events such as:

- pause
- track change
- queue change
- app/service shutdown opportunity
- occasional checkpoint for very long content if needed

Play-history writes should be event-based.

## MediaStore

Do not recursively crawl storage on every launch.

Do not continuously scan in the background.

Use one efficient query projection and only refresh when needed.

## No unnecessary libraries

Avoid adding libraries for tiny utilities.

Every dependency should justify:

- APK cost
- startup cost
- memory cost
- background behavior
- maintenance

## Release optimization

For release builds:

- R8/minification
- resource shrinking
- remove debug logging
- Baseline Profile
- Startup Profile
- no debug inspectors
- no unnecessary native libraries

## Measurement tools

Use:

- Android Studio profiler
- Perfetto
- Macrobenchmark
- Baseline Profile tooling
- `adb dumpsys`
- memory profiler
- CPU profiler
- Android battery/system metrics

## Acceptance targets

These are engineering targets, not hard promises:

### Startup
- warm launch: feels near-instant
- cold launch: no heavy synchronous scan before first frame

### Scrolling
- smooth with libraries of at least 10,000 songs
- artwork loading must not cause obvious jank

### Memory
- avoid unbounded growth
- no full-resolution album-art cache
- background playback should use much less memory than active browsing

### Screen-off playback
The app should show minimal CPU activity beyond what the audio pipeline requires.

## Real-device benchmark plan

Use the Pixel 7a.

Test combinations:

- MP3
- AAC
- FLAC
- Opus where supported
- speaker
- Bluetooth
- screen on
- screen off
- audio offload active
- audio offload inactive

For battery comparisons:

- same battery starting range
- fixed volume
- same tracks
- same output device
- airplane mode when appropriate
- multi-hour test
- repeat tests because battery measurements are noisy
