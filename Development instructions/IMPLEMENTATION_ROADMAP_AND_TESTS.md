# Implementation Roadmap and Tests

## Phase 1 — project skeleton

Create:

- Compose app
- Material 3 theme
- navigation shell
- permission flow for audio access
- app container/manual dependencies

Do not build visual polish yet.

Definition of done:

- installs on Pixel 7a
- launches without crash
- permission accepted/denied paths both work

## Phase 2 — playback engine

Implement first:

- `PlaybackService`
- ExoPlayer
- MediaSession
- MediaController connection
- play/pause
- previous/next
- seek
- queue
- shuffle
- repeat
- background playback
- lock-screen/media notification controls

Definition of done:

- start music
- leave app
- turn screen off
- playback continues correctly
- Bluetooth/headset buttons work
- Activity can be killed/recreated without losing active playback

## Phase 3 — MediaStore library

Implement:

- songs
- albums
- artists
- folders
- sort
- search

Definition of done:

- library reflects real device audio
- no recursive filesystem crawl
- no UI freeze during query
- empty/permission-denied states are handled

## Phase 4 — app-owned persistence

Add:

- favorites
- playlists
- queue/session restore
- user settings

Use Room only for relational app-owned state.

Definition of done:

- playlists survive restart
- favorites survive restart
- last playback context can be restored sensibly

## Phase 5 — final UI

Implement approved visual direction.

Screens:

- Songs
- Albums
- Artists
- Folders
- Search
- Mini Player
- Now Playing
- Queue
- Playlists
- Settings

Definition of done:

- minimum 48dp touch targets
- light/dark/system theme
- long titles handled
- TalkBack labels for icon-only controls
- no continuously running decorative animations

## Phase 6 — performance pass

Measure before optimizing.

Check:

- startup
- recompositions
- list jank
- bitmap allocations
- MediaStore query time
- database writes
- service wakeups
- CPU during screen-off playback
- memory after browsing many albums

Fix measurable problems only.

## Phase 7 — Baseline/Startup Profiles

Create benchmark/profile modules.

Profile critical journeys:

1. cold launch
2. open Songs
3. scroll Songs
4. open Albums
5. open Now Playing
6. open Search

Verify release behavior, not only debug builds.

## Functional test cases

### Permissions
- first launch permission grant
- permission denial
- permission revoked later

### Library
- no songs
- 1 song
- thousands of songs
- missing artwork
- weird metadata
- very long titles
- unknown artist/album
- deleted file
- changed MediaStore library

### Playback
- play/pause
- seek near start/end
- next/previous
- queue end
- repeat one/all/off
- shuffle
- remove current queue item
- reorder while playing

### Lifecycle
- rotate/recreate Activity
- home button
- screen off
- process under memory pressure
- reopen app during playback

### Audio routing
- speaker
- wired/USB if available
- Bluetooth connect/disconnect

### Interruptions
- another audio app starts
- call/communication interruption if testable
- headset disconnect

## Performance regression rules

Reject a change if it causes any of these without a strong feature reason:

- continuous background polling
- recurring storage scans
- large new native dependency
- obvious list jank
- large permanent memory increase
- high-frequency disk writes
- loss of audio offload for normal playback
- persistent GPU animation on Now Playing

## Coding style

- idiomatic Kotlin
- clear names
- small focused classes
- avoid clever abstractions
- avoid premature generic frameworks
- prefer composition over inheritance
- use suspend/Flow appropriately
- no `GlobalScope`
- no blocking I/O on main thread
- no giant god ViewModel

## Final release checklist

- release build installs
- no debug logging spam
- R8 enabled
- resources shrunk
- no unused permissions
- no internet permission unless a real feature requires it
- background playback works on Android 17
- notification/media controls work
- Bluetooth controls work
- large library scroll is smooth
- memory remains stable
- screen-off playback produces minimal app-side work
