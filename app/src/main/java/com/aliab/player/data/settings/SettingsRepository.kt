package com.aliab.player.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val SETTINGS_DATASTORE_NAME = "player_settings"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
	name = SETTINGS_DATASTORE_NAME,
)

/** The available appearance modes. */
enum class ThemeMode {
	SYSTEM,
	LIGHT,
	DARK,
}

/** The supported orders for the songs list. */
enum class SongSort {
	TITLE,
	ARTIST,
	ALBUM,
	DATE_ADDED,
}

/** Immutable settings consumed by the UI and future library/playback features. */
data class AppSettings(
	val themeMode: ThemeMode = ThemeMode.SYSTEM,
	val useDynamicColor: Boolean = true,
	val songSort: SongSort = SongSort.TITLE,
	val songsSortAscending: Boolean = true,
	val restorePlaybackOnLaunch: Boolean = true,
)

/**
 * Persists small user preferences. Values are written only after a user action and are exposed as
 * one immutable stream so callers can safely render the latest settings.
 */
class SettingsRepository(context: Context) {
	private val dataStore = context.applicationContext.settingsDataStore

	val settings: Flow<AppSettings> = dataStore.data
		.catch { error ->
			if (error is IOException) {
				emit(emptyPreferences())
			} else {
				throw error
			}
		}
		.map(::toAppSettings)
		.distinctUntilChanged()

	suspend fun setThemeMode(themeMode: ThemeMode) {
		dataStore.edit { preferences ->
			preferences[Keys.themeMode] = themeMode.name
		}
	}

	suspend fun setDynamicColorEnabled(enabled: Boolean) {
		dataStore.edit { preferences ->
			preferences[Keys.dynamicColorEnabled] = enabled
		}
	}

	suspend fun setSongSort(songSort: SongSort, ascending: Boolean) {
		dataStore.edit { preferences ->
			preferences[Keys.songSort] = songSort.name
			preferences[Keys.songsSortAscending] = ascending
		}
	}

	suspend fun setRestorePlaybackOnLaunch(enabled: Boolean) {
		dataStore.edit { preferences ->
			preferences[Keys.restorePlaybackOnLaunch] = enabled
		}
	}

	private fun toAppSettings(preferences: Preferences): AppSettings = AppSettings(
		themeMode = preferences[Keys.themeMode].toThemeMode(),
		useDynamicColor = preferences[Keys.dynamicColorEnabled] ?: true,
		songSort = preferences[Keys.songSort].toSongSort(),
		songsSortAscending = preferences[Keys.songsSortAscending] ?: true,
		restorePlaybackOnLaunch = preferences[Keys.restorePlaybackOnLaunch] ?: true,
	)

	private object Keys {
		val themeMode = stringPreferencesKey("theme_mode")
		val dynamicColorEnabled = booleanPreferencesKey("dynamic_color_enabled")
		val songSort = stringPreferencesKey("song_sort")
		val songsSortAscending = booleanPreferencesKey("songs_sort_ascending")
		val restorePlaybackOnLaunch = booleanPreferencesKey("restore_playback_on_launch")
	}
}

private fun String?.toThemeMode(): ThemeMode =
	this?.let { savedValue -> ThemeMode.entries.firstOrNull { it.name == savedValue } }
		?: ThemeMode.SYSTEM

private fun String?.toSongSort(): SongSort =
	this?.let { savedValue -> SongSort.entries.firstOrNull { it.name == savedValue } }
		?: SongSort.TITLE
