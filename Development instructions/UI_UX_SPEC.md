# UI / UX Specification

## Design direction

Use the approved minimalist Now Playing concept as the visual reference:

- monochrome
- premium
- lots of whitespace
- centered composition
- large square album art
- simple typography
- thin seek bar
- rounded playback control group
- static visuals
- no decorative motion loops

The implementation must be Android/Pixel-native rather than copying iPhone status/navigation chrome.

## Now Playing screen

Recommended hierarchy:

```text
System status bar

NOW PLAYING

[ large square album artwork ]

Song Title
Artist

────────●────────
2:34        3:45

[ shuffle | previous | play/pause | next | repeat ]

Lyrics      Queue      More   (optional small utility row)
```

Remove these concept-image elements:

- “At a Glance” chip
- date/status chip
- “Swipe for next/previous” instructional text
- “Now Playing history” pill

They add clutter without enough utility.

## Suggested dimensions

Use adaptive Compose sizing, but start with:

- horizontal screen padding: `20–24dp`
- top content spacing: `16–24dp`
- artwork corner radius: `12–16dp`
- artwork max width: roughly `min(screenWidth - 40dp, 420dp)`
- title top spacing: `20–24dp`
- title: `22–26sp`, medium
- artist: `14–16sp`
- control visual size: around `40–48dp`
- **minimum touch target: 48dp**
- control group vertical padding: `10–14dp`
- seek bar vertical touch area: at least `40–48dp` even if the visible track is thin

Do not over-space letters in real song titles. Use normal or mild tracking.

## Seek bar

The visible track can be thin.

Requirements:

- large invisible touch target
- thumb becomes more obvious while dragging
- current time left
- duration right
- update smoothly while screen is visible
- do not update the entire screen at high frequency

## Gestures

Optional:

- swipe artwork left/right: next/previous

Do not rely on hidden gestures for essential controls.

## Themes

Support:

- Light
- Dark
- System

Dark theme should be AMOLED-friendly:

- near-black/black background
- restrained gray surfaces
- high text contrast

Do not force pure black everywhere if it damages hierarchy.

## Artwork

Artwork behavior:

- decode at requested display size
- no full-resolution bitmap when showing a thumbnail
- show a simple placeholder if missing
- no continuously blurred artwork background

If background color extraction is added, compute/cache it when artwork changes. Do not run live GPU blur.

## Lists

For songs/albums/artists:

- use `LazyColumn` / `LazyVerticalGrid`
- stable item keys
- fixed/known artwork target size
- avoid expensive per-item animations
- do not put unnecessary nested scroll containers

## Accessibility

Must support:

- 48dp minimum touch targets
- meaningful content descriptions for icon-only controls
- readable contrast
- dynamic text without catastrophic clipping
- long titles with ellipsis or controlled marquee
- TalkBack-friendly control labels

## Animation policy

Allowed:

- short play/pause transition
- mini-player expand/collapse
- small state transitions

Avoid:

- rotating record
- animated waveform
- live spectrum
- particle effects
- continuous gradient animation
- live blur
