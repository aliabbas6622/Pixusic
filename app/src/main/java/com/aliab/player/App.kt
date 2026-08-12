package com.aliab.player

import android.app.Application
import android.content.Context
import com.aliab.player.data.media.MediaStoreRepository
import com.aliab.player.data.settings.SettingsRepository

/**
 * Application entry point and owner of process-wide app dependencies.
 *
 * Keeping dependencies here makes their lifetime explicit without adding a dependency-injection
 * framework to this small app.
 */
class PlayerApplication : Application() {
	lateinit var appContainer: AppContainer
		private set

	override fun onCreate() {
		super.onCreate()
		appContainer = AppContainer(applicationContext)
	}
}

/**
 * Dependencies shared by screens and Android components.
 *
 * Repositories that need to be introduced in later phases belong here so they have one clear,
 * application-scoped owner.
 */
class AppContainer(context: Context) {
	val settingsRepository = SettingsRepository(context)
	val mediaStoreRepository = MediaStoreRepository(context)

	private val playlistDatabase = com.aliab.player.data.playlists.PlaylistDatabase.getInstance(context)
	val playlistRepository = com.aliab.player.data.playlists.PlaylistRepository(playlistDatabase.playlistDao())
}
