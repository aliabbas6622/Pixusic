package com.aliab.player.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aliab.player.PlayerApplication
import com.aliab.player.playback.PlaybackViewModel
import com.aliab.player.playback.PlayerConnection
import com.aliab.player.ui.albums.AlbumDetailScreen
import com.aliab.player.ui.albums.AlbumsScreen
import com.aliab.player.ui.artists.ArtistDetailScreen
import com.aliab.player.ui.artists.ArtistsScreen
import com.aliab.player.ui.folders.FolderDetailScreen
import com.aliab.player.ui.folders.FoldersScreen
import com.aliab.player.ui.library.LibraryViewModel
import com.aliab.player.ui.nowplaying.NowPlayingScreen
import com.aliab.player.ui.queue.QueueScreen
import com.aliab.player.ui.songs.SongsScreen

private const val NOW_PLAYING_ROUTE = "nowplaying"
private const val QUEUE_ROUTE = "queue"
private const val ALBUM_ROUTE = "album"
private const val ARTIST_ROUTE = "artist"
private const val FOLDER_ROUTE = "folder"

/** Fast transition durations (160ms for ultra-responsive feel). */
private const val NAV_TRANSITION_DURATION_MS = 160

/** The four top-level, local-library destinations in the first release. */
enum class LibraryDestination(val route: String, val label: String, val icon: ImageVector) {
    Songs("songs", "Songs", Icons.Outlined.MusicNote),
    Albums("albums", "Albums", Icons.Outlined.Album),
    Artists("artists", "Artists", Icons.Outlined.Person),
    Folders("folders", "Folders", Icons.Outlined.Folder),

    ;

    companion object {
        fun fromRoute(route: String?): LibraryDestination =
            values().firstOrNull { it.route == route } ?: Songs
    }
}

/**
 * Root UI for the player with ultra-fast snappy screen transitions (160ms).
 */
@Composable
fun PlayerApp(
    audioPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlayerTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            if (audioPermissionGranted) {
                LibraryShell()
            } else {
                AudioPermissionRequired(onRequestPermission = onRequestPermission)
            }
        }
    }
}

@Composable
private fun LibraryShell(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as PlayerApplication).appContainer
    val libraryViewModel: LibraryViewModel = viewModel {
        LibraryViewModel(appContainer.mediaStoreRepository, appContainer.settingsRepository)
    }
    val playbackViewModel: PlaybackViewModel = viewModel {
        PlaybackViewModel(PlayerConnection(context.applicationContext))
    }
    val playbackState by playbackViewModel.uiState.collectAsStateWithLifecycle()
    val libraryLoading by libraryViewModel.isLoading.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedDestination = LibraryDestination.fromRoute(currentRoute)
    val isFullScreenRoute = currentRoute == NOW_PLAYING_ROUTE ||
        currentRoute == QUEUE_ROUTE ||
        currentRoute?.startsWith("$ALBUM_ROUTE/") == true ||
        currentRoute?.startsWith("$ARTIST_ROUTE/") == true ||
        currentRoute?.startsWith("$FOLDER_ROUTE/") == true

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isFullScreenRoute) {
                Column {
                    if (playbackState.isConnected && playbackState.currentSong != null) {
                        MiniPlayer(
                            state = playbackState,
                            onOpenNowPlaying = { navController.navigate(NOW_PLAYING_ROUTE) },
                            onTogglePlayPause = playbackViewModel::togglePlayPause,
                        )
                    }
                    LibraryNavigationBar(
                        selectedDestination = selectedDestination,
                        onDestinationSelected = { destination ->
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LibraryDestination.Songs.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        NAV_TRANSITION_DURATION_MS,
                        easing = LinearOutSlowInEasing,
                    ),
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        NAV_TRANSITION_DURATION_MS,
                        easing = FastOutLinearInEasing,
                    ),
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        NAV_TRANSITION_DURATION_MS,
                        easing = LinearOutSlowInEasing,
                    ),
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        NAV_TRANSITION_DURATION_MS,
                        easing = FastOutLinearInEasing,
                    ),
                )
            },
        ) {
            LibraryDestination.values().forEach { destination ->
                composable(destination.route) {
                    when (destination) {
                        LibraryDestination.Songs -> {
                            val songs by libraryViewModel.songs.collectAsStateWithLifecycle()
                            val currentSort by libraryViewModel.currentSort.collectAsStateWithLifecycle()
                            val isAscending by libraryViewModel.isAscending.collectAsStateWithLifecycle()

                            SongsScreen(
                                songs = songs,
                                isLoading = libraryLoading,
                                contentPadding = innerPadding,
                                onSongClick = { index -> playbackViewModel.playQueue(songs, index) },
                                onAddNext = { song -> playbackViewModel.addNext(song) },
                                onAddToQueueEnd = { song -> playbackViewModel.addToQueueEnd(song) },
                                currentSort = currentSort,
                                isAscending = isAscending,
                                onSortChanged = libraryViewModel::setSongSort,
                            )
                        }
                        LibraryDestination.Albums -> {
                            val albums by libraryViewModel.albums.collectAsStateWithLifecycle()
                            AlbumsScreen(
                                albums = albums,
                                isLoading = libraryLoading,
                                contentPadding = innerPadding,
                                onAlbumClick = { albumId ->
                                    navController.navigate("$ALBUM_ROUTE/$albumId")
                                },
                            )
                        }
                        LibraryDestination.Artists -> {
                            val artists by libraryViewModel.artists.collectAsStateWithLifecycle()
                            ArtistsScreen(
                                artists = artists,
                                isLoading = libraryLoading,
                                contentPadding = innerPadding,
                                onArtistClick = { artist ->
                                    navController.navigate("$ARTIST_ROUTE/${Uri.encode(artist.name)}")
                                },
                            )
                        }
                        LibraryDestination.Folders -> {
                            val folders by libraryViewModel.folders.collectAsStateWithLifecycle()
                            FoldersScreen(
                                folders = folders,
                                isLoading = libraryLoading,
                                contentPadding = innerPadding,
                                onFolderClick = { folder ->
                                    navController.navigate("$FOLDER_ROUTE/${Uri.encode(folder.path)}")
                                },
                            )
                        }
                    }
                }
            }

            composable(
                route = NOW_PLAYING_ROUTE,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(180, easing = LinearOutSlowInEasing),
                    ) + fadeIn(tween(180))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(160, easing = FastOutLinearInEasing),
                    ) + fadeOut(tween(160))
                },
            ) {
                val favoriteSongIds by libraryViewModel.favoriteSongIds.collectAsStateWithLifecycle()
                val currentSongId = playbackState.currentSong?.id ?: -1L
                val isCurrentFavorite = remember(favoriteSongIds, currentSongId) {
                    favoriteSongIds.contains(currentSongId)
                }

                NowPlayingScreen(
                    state = playbackState,
                    onBack = { navController.popBackStack() },
                    onTogglePlayPause = playbackViewModel::togglePlayPause,
                    onNext = playbackViewModel::skipToNext,
                    onPrevious = playbackViewModel::skipToPrevious,
                    onSeek = playbackViewModel::seekTo,
                    onShuffleChanged = playbackViewModel::setShuffleEnabled,
                    onRepeatChanged = playbackViewModel::setRepeatMode,
                    onPositionUpdatesEnabled = playbackViewModel::setPositionUpdatesEnabled,
                    onOpenQueue = { navController.navigate(QUEUE_ROUTE) },
                    isFavorite = isCurrentFavorite,
                    onToggleFavorite = {
                        if (currentSongId > 0L) {
                            libraryViewModel.toggleFavorite(currentSongId)
                        }
                    },
                )
            }

            composable(
                route = QUEUE_ROUTE,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(180, easing = LinearOutSlowInEasing),
                    ) + fadeIn(tween(180))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(160, easing = FastOutLinearInEasing),
                    ) + fadeOut(tween(160))
                },
            ) {
                val allSongs by libraryViewModel.songs.collectAsStateWithLifecycle()
                QueueScreen(
                    state = playbackState,
                    allSongs = allSongs,
                    onBack = { navController.popBackStack() },
                    onSelectQueueItem = { index -> playbackViewModel.seekToQueueItem(index) },
                    onRemoveQueueItem = { index -> playbackViewModel.removeQueueItem(index) },
                    onClearQueue = { playbackViewModel.clearQueue() },
                    onAddNext = { song -> playbackViewModel.addNext(song) },
                    onAddToQueueEnd = { song -> playbackViewModel.addToQueueEnd(song) },
                )
            }

            composable(
                route = "$ALBUM_ROUTE/{albumId}",
                arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
            ) { entry ->
                val albumId = entry.arguments?.getLong("albumId") ?: -1L
                val songs by libraryViewModel.songs.collectAsStateWithLifecycle()
                val tracks = remember(albumId, songs) {
                    songs.filter { it.albumId == albumId }
                        .sortedWith(
                            compareBy({ it.trackNumber ?: Int.MAX_VALUE }, { it.title.lowercase() }),
                        )
                }
                AlbumDetailScreen(
                    albumId = albumId,
                    tracks = tracks,
                    onBack = { navController.popBackStack() },
                    onTrackClick = { index -> playbackViewModel.playQueue(tracks, index) },
                )
            }

            composable(
                route = "$ARTIST_ROUTE/{artistName}",
                arguments = listOf(navArgument("artistName") { type = NavType.StringType }),
            ) { entry ->
                val artistName = entry.arguments?.getString("artistName").orEmpty()
                val songs by libraryViewModel.songs.collectAsStateWithLifecycle()
                val artistTracks = remember(artistName, songs) {
                    libraryViewModel.songsForArtist(artistName)
                }
                ArtistDetailScreen(
                    artistName = artistName,
                    tracks = artistTracks,
                    onBack = { navController.popBackStack() },
                    onTrackClick = { index -> playbackViewModel.playQueue(artistTracks, index) },
                )
            }

            composable(
                route = "$FOLDER_ROUTE/{folderPath}",
                arguments = listOf(navArgument("folderPath") { type = NavType.StringType }),
            ) { entry ->
                val folderPath = entry.arguments?.getString("folderPath").orEmpty()
                val folderSongs by libraryViewModel.folderSongs.collectAsStateWithLifecycle()
                val folderLoading by libraryViewModel.folderLoading.collectAsStateWithLifecycle()
                LaunchedEffect(folderPath) { libraryViewModel.loadFolderSongs(folderPath) }
                FolderDetailScreen(
                    folderName = folderPath.substringAfterLast('/'),
                    songs = folderSongs,
                    isLoading = folderLoading,
                    onBack = { navController.popBackStack() },
                    onTrackClick = { index -> playbackViewModel.playQueue(folderSongs, index) },
                )
            }
        }
    }
}

@Composable
private fun LibraryNavigationBar(
    selectedDestination: LibraryDestination,
    onDestinationSelected: (LibraryDestination) -> Unit,
) {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        LibraryDestination.values().forEach { destination ->
            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        modifier = Modifier.size(22.dp),
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun AudioPermissionRequired(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp),
            )
        }

        Text(
            text = "Your music,\non your device.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = "Allow access to audio so Player can build your local library. Nothing leaves your device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp),
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp)
                .padding(top = 36.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = "Allow audio access",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "You can change this later in Android settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}
