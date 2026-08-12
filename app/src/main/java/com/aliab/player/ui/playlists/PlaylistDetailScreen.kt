package com.aliab.player.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliab.player.data.playlists.PlaylistWithSongCount
import com.aliab.player.model.Song
import com.aliab.player.ui.artwork.AlbumArt
import com.aliab.player.ui.songs.SongOverflowMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistWithSongCount?,
    songs: List<Song>,
    contentPadding: PaddingValues,
    onBackClick: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onSongClick: (Int) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onRemoveFromPlaylist: (Song) -> Unit,
    onShare: ((Song) -> Unit)? = null,
    onShowDetails: ((Song) -> Unit)? = null,
    onDeleteSong: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (playlist == null) {
        Box(modifier = modifier.fillMaxSize().padding(contentPadding))
        return
    }

    Scaffold(
        modifier = modifier.padding(contentPadding),
        topBar = {
            TopAppBar(
                title = { Text("PLAYLIST", style = MaterialTheme.typography.labelMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = playlist.playlist.name,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${songs.size} ${if (songs.size == 1) "song" else "songs"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onPlayAll,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Play All")
                }

                FilledTonalButton(onClick = onShuffle) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Shuffle")
                }
            }

            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Playlist is empty",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(songs) { index, song ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = song.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = song.artist,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingContent = {
                                AlbumArt(
                                    albumId = song.albumId,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    shape = RoundedCornerShape(4.dp),
                                )
                            },
                            trailingContent = {
                                SongOverflowMenu(
                                    onPlayNext = { onPlayNext(song) },
                                    onAddToQueue = { onAddToQueue(song) },
                                    onShare = onShare?.let { cb -> { cb(song) } },
                                    onDetails = onShowDetails?.let { cb -> { cb(song) } },
                                    onDelete = onDeleteSong?.let { cb -> { cb(song) } },
                                    extraItems = {
                                        DropdownMenuItem(
                                            text = { Text("Remove from Playlist") },
                                            onClick = { onRemoveFromPlaylist(song) }
                                        )
                                    },
                                )
                            },
                            modifier = Modifier.clickable { onSongClick(index) }
                        )
                    }
                }
            }
        }
    }
}
