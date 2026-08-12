package com.aliab.player.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
	val favoriteSongIds: Set<Long> = emptySet(),
	val lastQueueSongIds: List<Long> = emptyList(),
	val lastQueueIndex: Int = 0,
	val lastQueuePositionMs: Long = 0L,
)

/**
 * Persists user preferences and favorites. Values are written after user actions.
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

	suspend fun toggleFavorite(songId: Long) {
		dataStore.edit { preferences ->
			val current = preferences[Keys.favoriteSongIds] ?: emptySet()
			val stringId = songId.toString()
			val updated = if (current.contains(stringId)) {
				current - stringId
			} else {
				current + stringId
			}
			preferences[Keys.favoriteSongIds] = updated
		}
	}

	suspend fun saveLastQueue(songIds: List<Long>, currentIndex: Int, positionMs: Long) {
		dataStore.edit { prefs ->
			prefs[Keys.lastQueueSongIds] = songIds.map { it.toString() }.toSet()
			prefs[Keys.lastQueueIndex] = currentIndex
			prefs[Keys.lastQueuePositionMs] = positionMs
		}
	}

	suspend fun clearLastQueue() {
		dataStore.edit { prefs ->
			prefs.remove(Keys.lastQueueSongIds)
			prefs.remove(Keys.lastQueueIndex)
			prefs.remove(Keys.lastQueuePositionMs)
		}
	}

	private fun toAppSettings(preferences: Preferences): AppSettings = AppSettings(
		themeMode = preferences[Keys.themeMode].toThemeMode(),
		useDynamicColor = preferences[Keys.dynamicColorEnabled] ?: true,
		songSort = preferences[Keys.songSort].toSongSort(),
		songsSortAscending = preferences[Keys.songsSortAscending] ?: true,
		restorePlaybackOnLaunch = preferences[Keys.restorePlaybackOnLaunch] ?: true,
		favoriteSongIds = (preferences[Keys.favoriteSongIds] ?: emptySet()).mapNotNull { it.toLongOrNull() }.toSet(),
		lastQueueSongIds = (preferences[Keys.lastQueueSongIds] ?: emptySet()).mapNotNull { it.toLongOrNull() },
		lastQueueIndex = preferences[Keys.lastQueueIndex] ?: 0,
		lastQueuePositionMs = preferences[Keys.lastQueuePositionMs] ?: 0L,
	)

	private object Keys {
		val themeMode = stringPreferencesKey("theme_mode")
		val dynamicColorEnabled = booleanPreferencesKey("dynamic_color_enabled")
		val songSort = stringPreferencesKey("song_sort")
		val songsSortAscending = booleanPreferencesKey("songs_sort_ascending")
		val restorePlaybackOnLaunch = booleanPreferencesKey("restore_playback_on_launch")
		val favoriteSongIds = stringSetPreferencesKey("favorite_song_ids")
		val lastQueueSongIds = stringSetPreferencesKey("last_queue_song_ids")
		val lastQueueIndex = intPreferencesKey("last_queue_index")
		val lastQueuePositionMs = longPreferencesKey("last_queue_position_ms")
	}
}

private fun String?.toThemeMode(): ThemeMode =
	this?.let { savedValue -> ThemeMode.entries.firstOrNull { it.name == savedValue } }
		?: ThemeMode.SYSTEM

private fun String?.toSongSort(): SongSort =
	this?.let { savedValue -> SongSort.entries.firstOrNull { it.name == savedValue } }
		?: SongSort.TITLE
