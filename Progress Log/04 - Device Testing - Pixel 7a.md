# 04 - Device Testing - Pixel 7a (wireless adb)

Wireless debugging (`adb pair`/`adb connect`) was used; device shows as `adb-<id>._adb-tls-connect._tcp`, model `Pixel_7a` (lynx).

## Results (debug build)

| Check | Result |
|---|---|
| Install | Streamed Install — Success |
| Cold launch | ~800 ms, Status ok |
| Crash check | None (no FATAL in logcat) |
| Permission denied | Permission screen with "Allow audio access" button |
| After `pm grant READ_MEDIA_AUDIO` | Library shell renders (header + 4 tabs) |
| Tabs | Songs / Albums / Artists / Folders present |
| Empty state | "No songs yet / Your music will appear here after the library is scanned." |

Screenshots saved to project root: `screenshot_permission.png`, `screenshot_library.png`.

## Phase 3 library + playback verification (real library on device)

| Check | Result |
|---|---|
| Songs tab | 116 songs with artwork + durations; tap plays the list from that index |
| Albums tab | 5 albums (grid); album detail shows ordered track list; tap track -> PLAYING |
| Artists tab | 1 artist group (all device tracks untagged), 129 songs; tap plays discography |
| Folders tab | 3 folders (download 42, WhatsApp Audio 72, WhatsApp Documents 2); detail -> PLAYING |
| Now Playing | controls + seek bar; position advances linearly (Slider seek-loop fixed) |
| Media notification | transport category, 3 actions, foreground service |
| Folder path with space | "WhatsApp Audio" opened via Uri-encoded nav route, 72 songs, no crash |

## Gotchas

- In Git Bash, `adb shell cat /sdcard/...` mangles the path; set `MSYS_NO_PATHCONV=1` (or use `//sdcard/...`).
- `adb devices` shows nothing until the phone accepts the pairing/authorization prompt.
- No AVD or system image was installed at the time; only the physical Pixel 7a was used.
