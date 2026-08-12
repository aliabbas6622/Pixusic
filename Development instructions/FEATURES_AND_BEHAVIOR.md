# Features and Behavior

## Library screens

### Songs

Show:

- artwork thumbnail
- title
- artist
- optional duration
- overflow menu

Actions:

- tap: play song in current sorted library context
- long press / overflow:
  - play next
  - add to queue
  - add to playlist
  - favorite/unfavorite
  - song info

Sorting:

- title
- artist
- album
- duration
- date added / modified where available

### Albums

Grid or compact list.

Album details:

- cover
- album name
- album artist
- year if available
- track count
- tracks ordered by disc + track number

### Artists

Artist details:

- artist name
- albums
- songs

### Folders

Provide an actual folder-style browser using MediaStore path metadata where possible.

Allow folder exclusion from the library.

### Search

Search should be local and fast.

Search across:

- songs
- artists
- albums

Do not perform web searches.

## Playback behavior

Required commands:

- play
- pause
- seek
- previous
- next
- shuffle
- repeat off
- repeat all
- repeat one

Follow standard Media3 semantics for edge cases.

## Queue

Queue is a first-class feature.

Required:

- display upcoming items
- reorder
- remove
- play any queue item
- add next
- add to end
- clear queue
- preserve queue across Activity recreation

Persist last queue/session state if it can be done without frequent writes.

Do not write playback position to disk every second.

## Favorites

Favorite state is app-owned data stored in Room or another small local persistence layer.

## Playlists

User-created local playlists:

- create
- rename
- delete
- add tracks
- remove tracks
- reorder tracks

Do not depend on a cloud account.

## Resume behavior

Store enough state to restore:

- last queue
- current queue index
- shuffle state
- repeat mode
- last song

Persist playback position on meaningful lifecycle points, not constantly.

## Audio focus and interruptions

Expected behavior:

- pause or duck according to platform/media conventions
- respond correctly to calls/navigation prompts/other audio
- resume only when appropriate
- react to headset/Bluetooth disconnect safely

Prefer Media3 defaults unless testing exposes a specific problem.

## Optional V1.1 features

Only after V1 is stable:

- embedded lyrics
- `.lrc` synced lyrics
- sleep timer
- recently played
- most played
- ignore very short audio
- folder blacklist
- richer song info

## Future advanced features

Later only:

- gapless tuning
- ReplayGain
- equalizer
- crossfade
- playback speed
- pitch control
- Android Auto
- Chromecast

Any feature that disables audio offload or creates continuous processing must be optional.
