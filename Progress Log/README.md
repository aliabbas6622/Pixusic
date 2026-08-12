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
| MediaStore library (Phase 3) | Done — Songs, Albums (+detail), Artists (+detail), Folders (+detail), device-verified |
| Queue & Search (Phase 5+) | **Done** — Queue view (`QueueScreen`), instant search filtering (`SongsScreen`) |
| Transitions & Speed | **Done** — Ultra-fast 160ms–180ms transitions enabled across `NavHost` (see `08`) |
| App persistence (Phase 4) | Settings only; Room DB for Favorites / Playlists pending |
| Final UI (Phase 5) | **Done** — full visual-polish pass complete (see `07` & `08`); reference aligned |
| Performance pass (Phase 6) | Not started |
| Baseline/Startup profiles (Phase 7) | Not started |

## What's next (recommended order)

1. **Room Database Persistence (Phase 4)** — `Favorite`, `Playlist`, `PlaylistEntry`, `PlayHistory` tables for favorites & user playlists.
2. **Strings Migration** — migrate hardcoded UI strings from Kotlin to `strings.xml`.
3. **Sort UI Options** — add popup/sheet to change sorting options (Title, Artist, Album, Date Added).
4. **Code Review Items** — wake mode check (#3), permission re-check on resume (#5).
