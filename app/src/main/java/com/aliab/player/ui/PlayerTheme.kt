package com.aliab.player.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.aliab.player.data.settings.ThemeMode

// ── Light theme ────────────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary                = PlayerBlack,
    onPrimary              = PlayerWhite,
    primaryContainer       = PlayerSurfaceVariant,
    onPrimaryContainer     = PlayerBlack,
    secondary              = PlayerMuted,
    onSecondary            = PlayerWhite,
    secondaryContainer     = PlayerSurfaceVariant,
    onSecondaryContainer   = PlayerBlack,
    background             = PlayerWhite,
    onBackground           = PlayerBlack,
    surface                = PlayerWhite,
    onSurface              = PlayerBlack,
    surfaceVariant         = PlayerSurfaceVariant,
    onSurfaceVariant       = PlayerMuted,
    outline                = PlayerOutline,
    outlineVariant         = PlayerOutline,
)

// ── Dark theme — AMOLED-friendly ───────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary                = PlayerWhite,
    onPrimary              = PlayerBlack,
    primaryContainer       = PlayerSurfaceDark,
    onPrimaryContainer     = PlayerWhite,
    secondary              = PlayerMutedLight,
    onSecondary            = PlayerBlack,
    secondaryContainer     = PlayerSurfaceDark,
    onSecondaryContainer   = PlayerWhite,
    background             = PlayerBlack,
    onBackground           = PlayerWhite,
    surface                = PlayerBlack,
    onSurface              = PlayerWhite,
    surfaceVariant         = PlayerSurfaceDark,
    onSurfaceVariant       = PlayerMutedLight,
    outline                = PlayerOutlineDark,
    outlineVariant         = PlayerOutlineDark,
)

@Composable
fun PlayerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = PlayerTypography,
        content     = content,
    )
}
