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
| Queue & Search (Phase 5+) | Done — Queue view (`QueueScreen`), instant search filtering (`SongsScreen`) |
| Transitions & Speed | Done — Ultra-fast 160ms–180ms transitions enabled across `NavHost` (see `08`) |
| Favorites System (Phase 4) | **Done** — DataStore persistent favorites + Heart toggle in Now Playing (see `09`) |
| Song Sort UI Options | **Done** — Dialog to sort by Title, Artist, Album, Date Added + Asc/Desc toggle (see `09`) |
| Queue management | **Done** — two-tab queue screen (Now Playing / Add Songs) + per-song ⋮ Play Next / Add to Queue (see `11`) |
| Junk audio auto-filter | **Done** — WhatsApp voice notes (`AUD-…`), short clips & system sounds filtered at query time across all tabs; 129→42 songs verified on-device (see `11`) |
| Now Playing layout | **Done** — vertically balanced + system-bar insets (see `10`) |
| GitHub | **Live** — public repo `aliabbas6622/Pixusic` (see `10`) |
| Code Review Items | **Done** — Fixed #5 (onResume permission re-check in `MainActivity`) |
| Performance pass (Phase 6) | Not started |
| Baseline/Startup profiles (Phase 7) | Not started |

## What's next (recommended order)

1. **Strings Migration** — migrate hardcoded UI strings from Kotlin to `strings.xml`.
2. **User Playlists** — create local user playlists (create, add tracks, remove tracks).
3. **Folder Blacklist / Exclusion** — allow users to exclude specific folders from library scanning.
