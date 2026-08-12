package com.aliab.player.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Colour tokens ──────────────────────────────────────────────────────────────
internal val PlayerBlack          = Color(0xFF0E0E0F)   // near-black, slight warmth
internal val PlayerWhite          = Color(0xFFFAFAFA)   // near-white, never blinding
internal val PlayerMuted          = Color(0xFF7A7676)   // medium grey for subtitles (light mode)
internal val PlayerMutedLight     = Color(0xFFBBB6B6)   // lighter grey for subtitles (dark mode)
internal val PlayerSurfaceVariant = Color(0xFFF0ECEC)   // light card / container surface
internal val PlayerSurfaceDark    = Color(0xFF1C1B1B)   // dark card / container surface
internal val PlayerOutline        = Color(0xFFDED9D9)   // dividers / borders (light)
internal val PlayerOutlineDark    = Color(0xFF3A3737)   // dividers / borders (dark)

// Accent for active states on control buttons (shuffle on / repeat on)
internal val PlayerAccentLight    = Color(0xFF111111)   // same as black in light mode
internal val PlayerAccentDark     = Color(0xFFEEEAEA)   // near-white in dark mode

// ── Typography ─────────────────────────────────────────────────────────────────
// We rely on the system sans-serif (Roboto on stock Android, which is clean and
// geometric). Switching to a downloadable Outfit / Inter font is straightforward
// later, but avoids an extra network dependency for now.
internal val PlayerTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,   // wide tracking for ALL-CAPS labels
    ),
    labelMedium = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp,
    ),
)
