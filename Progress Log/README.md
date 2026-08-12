# Progress Log

Handoff documentation for the **Player** Android music player (Pixel 7a target).

Every completed phase, update, and feature gets a numbered markdown file here so a future developer can pick up the project without re-deriving its state.

## How to use

- Read `README.md` for the current status.
- Each numbered file documents one area (setup/tooling, phase, testing, review).
- When a new phase is completed, add a new file and update the status table below.
- Reference actual file paths (backticks) so continuation work is mechanical.

## Current status

| Area | Status |
|---|---|
| Project skeleton (Phase 1) | Done — permission flow, theme, nav shell, manual DI |
| Playback engine (Phase 2) | Done — service + controller + queue ops + mini player + Now Playing, device-verified |
| MediaStore library (Phase 3) | Done — Songs, Albums (+detail), Artists, Folders (+detail), device-verified. Search & artist detail pending |
| App persistence (Phase 4) | Settings only; favorites / playlists / queue restore not started |
| Final UI (Phase 5) | **Done** — full visual-polish pass complete (see `07`); composable signatures unchanged |
| Performance pass (Phase 6) | Not started |
| Baseline/Startup profiles (Phase 7) | Not started |

## What's next (recommended order)

1. **Search** — add `MediaStoreRepository.search()` + `SearchScreen.kt` + nav route.
2. **Artist detail screen** — tapping an artist currently plays the full discography; a proper detail list is pending.
3. **Queue screen** — wire the "Queue" button in Now Playing to a real bottom-sheet or nav destination.
4. **Favorites / Playlists / Queue restore** (Phase 4, Room) — `Favorite`, `Playlist`, `PlaylistEntry`, `PlayHistory` tables.
5. **Strings** — migrate hardcoded UI strings from Kotlin to `strings.xml`.
6. **Downloadable font** — `PlayerThemeValues.kt` is ready; add `FontFamily` (e.g. Outfit via Google Fonts).
7. **Sort UI** — expose the sort fields already planned in the feature spec.
8. Fix remaining issues listed in `05 - Code Review Findings.md` (#3 wake mode, #5 permission re-check on resume, #6 minors).
