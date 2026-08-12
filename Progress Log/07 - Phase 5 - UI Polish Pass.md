# 07 - Phase 5 - UI Polish Pass

Status: **Complete — all screens redesigned and confirmed to compile (BUILD SUCCESSFUL).**

## What was done

The UI AI performed a full visual-polish pass over every composable under `ui/`. The backend (Phase 2 + 3) was untouched. All public composable signatures remain identical to those documented in `06`.

---

## Design direction

Matched the approved reference image (`Development instructions/minimal_music_player_ui.webp`):

- Monochrome palette (near-black `#0E0E0F` / near-white `#FAFAFA`)
- Large square album art with soft drop shadow
- Thin seek bar with wide touch target
- Rounded playback-control pill (neumorphic surface)
- Static visuals — no rotating record, no waveform, no continuous animation
- AMOLED-friendly dark theme (pure-black background)

---

## Files changed

### Design system

| File | Change |
|---|---|
| `ui/PlayerThemeValues.kt` | Refined colour palette; 11-level `Typography` scale with tight letter-spacing for headlines (`−0.3 sp`) and wide tracking for ALL-CAPS labels (`+1.5 sp`). Fills `secondaryContainer` / `outlineVariant` tokens. |
| `ui/PlayerTheme.kt` | Light + AMOLED dark colour schemes; `outlineVariant` added. |

### Now Playing

| File | Change |
|---|---|
| `ui/nowplaying/NowPlayingScreen.kt` | Large album art with `shadow(elevation=24.dp)`; seek bar uses `SliderDefaults.colors` for a monochrome thin track; control group wrapped in a `surfaceVariant` `Surface` with `RoundedCornerShape(64.dp)` (pill); shuffle/repeat tints animated via `animateColorAsState`; **Lyrics \| Queue \| More** utility row with pipe separators. |

### Mini Player

| File | Change |
|---|---|
| `ui/MiniPlayer.kt` | Animated 2 dp progress line at the top edge of the bar (500 ms `LinearEasing` via `animateFloatAsState`); refined icon sizing; `titleSmall` + `bodySmall` text hierarchy. |

### Library screens

| File | Change |
|---|---|
| `ui/songs/SongsScreen.kt` | Inset `HorizontalDivider` (0.5 dp, starts at 80 dp); `RoundedCornerShape(10.dp)` thumbnails; `titleSmall` / `bodySmall` text hierarchy. |
| `ui/albums/AlbumsScreen.kt` | Per-card `shadow(elevation=6.dp)` on artwork; `RoundedCornerShape(14.dp)`; refined spacing (`20 dp` vertical gap). |
| `ui/albums/AlbumDetailScreen.kt` | Centred artwork with `shadow(elevation=16.dp)`; `HorizontalDivider` above track list; inset track dividers starting at track-number column. |
| `ui/artists/ArtistsScreen.kt` | Inset dividers; `CircleShape` avatar `46 dp`; consistent `titleSmall` / `bodySmall`. |
| `ui/folders/FoldersScreen.kt` | Mirrors ArtistsScreen pattern with `RoundedCornerShape(12.dp)` folder icon. |
| `ui/folders/FolderDetailScreen.kt` | Larger folder icon header (`52 dp`, `14 dp` radius); full-width divider above track list; inset row dividers. |

### Shell & navigation

| File | Change |
|---|---|
| `ui/PlayerApp.kt` | Nav bar: thin `HorizontalDivider` (0.5 dp) replaces Material3 tonal elevation shadow; `22 dp` icon size; `labelSmall` nav labels. Permission screen: `MusicNote` icon anchor (48 dp) above headline; `bodyLarge` description text. |

---

## What was NOT changed

- All playback logic (`playback/`, `data/`, `model/`) — untouched.
- All public composable function signatures — identical to those in `06`.
- Navigation routes / arguments — identical.
- Gradle configuration — untouched.

---

## Known gaps / next

- **Search** screen not yet built (no UI or repo query exists).
- **Artist detail** screen: tapping an artist still plays the full discography; a detail list screen is pending.
- **Queue screen**: the `Queue` button in the Now Playing utility row is a label only — no destination yet.
- **Lyrics / Sort UI / Folder exclusion** — future phases.
- **Strings**: most UI strings are still hardcoded in Kotlin; migration to `strings.xml` pending.
- **Downloadable font** (e.g. Outfit/Inter via Google Fonts): the typography system is ready for it — just add the `FontFamily` to `PlayerThemeValues.kt` and pass it to `PlayerTypography`.
