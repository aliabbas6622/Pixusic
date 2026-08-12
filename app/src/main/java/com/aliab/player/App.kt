package com.aliab.player

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import okio.Path.Companion.toOkioPath
import com.aliab.player.data.lyrics.LyricsRepository
import com.aliab.player.data.media.MediaStoreRepository
import com.aliab.player.data.settings.SettingsRepository

/**
 * Application entry point and owner of process-wide app dependencies.
 *
 * Keeping dependencies here makes their lifetime explicit without adding a dependency-injection
 * framework to this small app.
 */
class PlayerApplication : Application(), SingletonImageLoader.Factory {
	lateinit var appContainer: AppContainer
		private set

	override fun onCreate() {
		super.onCreate()
		appContainer = AppContainer(applicationContext)
		// Coil's singleton is created lazily on first use; wiring our factory first guarantees the
		// tuned memory/disk caches are used everywhere album art is loaded.
		SingletonImageLoader.setSafe(this)
	}

	override fun newImageLoader(context: Context): ImageLoader =
		ImageLoader.Builder(context)
			.memoryCache {
				MemoryCache.Builder()
					// ~15% of app heap (down from Coil's default 25%) keeps the artwork cache
					// compact while still covering every album on screen.
					.maxSizePercent(context, 0.15)
					.build()
			}
			.diskCache {
				DiskCache.Builder()
					.directory(context.cacheDir.resolve("image_cache").toOkioPath())
					.maxSizeBytes(128L * 1024 * 1024)
					.build()
			}
			.build()
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
	val lyricsRepository = LyricsRepository(context)

	private val playlistDatabase = com.aliab.player.data.playlists.PlaylistDatabase.getInstance(context)
	val playlistRepository = com.aliab.player.data.playlists.PlaylistRepository(playlistDatabase.playlistDao())
}
