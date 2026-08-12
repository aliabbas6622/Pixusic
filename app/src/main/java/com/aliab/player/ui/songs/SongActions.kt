package com.aliab.player.ui.songs

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliab.player.model.Song
import com.aliab.player.ui.formatDisplayName
import com.aliab.player.ui.formatTime

/** Sends the audio file to another app (WhatsApp, Bluetooth, files, …) via the system share sheet. */
fun shareSong(context: Context, song: Song) {
	val sendIntent = Intent(Intent.ACTION_SEND).apply {
		type = song.mimeType ?: "audio/*"
		putExtra(Intent.EXTRA_STREAM, song.uri)
		putExtra(Intent.EXTRA_TITLE, formatDisplayName(song.title))
		addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	}
	context.startActivity(Intent.createChooser(sendIntent, "Share song"))
}

/**
 * Owns the [MediaStore.createDeleteRequest] flow (API 30+). The system shows a confirmation
 * dialog before anything is deleted; on confirm [deleted] fires with the song so the caller can
 * refresh the library and drop it from the live queue. On API 29 the legacy direct delete is
 * attempted as a fallback.
 */
@Composable
fun rememberSongDeleteLauncher(onDeleted: (Song) -> Unit): (Song) -> Unit {
	val context = LocalContext.current
	var pendingSong by remember { mutableStateOf<Song?>(null) }

	// API 34+ exposes createDeleteRequest as a PendingIntent; API 30–33 returned an Intent that
	// was removed from the SDK 34+ stubs, so it is reached via reflection there.
	val senderLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartIntentSenderForResult(),
	) { result ->
		val song = pendingSong
		pendingSong = null
		if (song != null && result.resultCode == Activity.RESULT_OK) {
			onDeleted(song)
		}
	}
	val intentLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult(),
	) { result ->
		val song = pendingSong
		pendingSong = null
		if (song != null && result.resultCode == Activity.RESULT_OK) {
			onDeleted(song)
		}
	}

	return remember(senderLauncher, intentLauncher, onDeleted) {
		{ song ->
			when {
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
					pendingSong = song
					val pendingIntent = MediaStore.createDeleteRequest(
						context.contentResolver,
						listOf(song.uri),
					)
					senderLauncher.launch(
						IntentSenderRequest.Builder(pendingIntent.intentSender).build(),
					)
				}

				Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
					val intent = runCatching {
						MediaStore::class.java
							.getMethod(
								"createDeleteRequest",
								ContentResolver::class.java,
								Collection::class.java,
							)
							.invoke(null, context.contentResolver, listOf(song.uri)) as? Intent
					}.getOrNull()
					if (intent != null) {
						pendingSong = song
						intentLauncher.launch(intent)
					} else {
						directDeleteWithFeedback(context, song, onDeleted)
					}
				}

				else -> directDeleteWithFeedback(context, song, onDeleted)
			}
		}
	}
}

private fun directDeleteWithFeedback(context: Context, song: Song, onDeleted: (Song) -> Unit) {
	val deleted = runCatching {
		context.contentResolver.delete(song.uri, null, null)
	}.getOrDefault(0)
	if (deleted > 0) {
		onDeleted(song)
	} else {
		Toast.makeText(context, "Couldn't delete this song", Toast.LENGTH_SHORT).show()
	}
}

/** Pop-up with the track's stored metadata. */
@Composable
fun SongDetailsDialog(song: Song, onDismiss: () -> Unit) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(
				text = formatDisplayName(song.title),
				style = MaterialTheme.typography.titleLarge,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
			)
		},
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				DetailsRow("Artist", formatDisplayName(song.artist))
				DetailsRow("Album", formatDisplayName(song.album))
				DetailsRow("Duration", formatTime(song.durationMs))
				song.trackNumber?.let { DetailsRow("Track", it.toString()) }
				song.year?.let { DetailsRow("Year", it.toString()) }
				DetailsRow("File", song.uri.lastPathSegment ?: "—")
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) { Text("Close") }
		},
	)
}

@Composable
private fun DetailsRow(label: String, value: String) {
	Row(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.weight(0.3f),
		)
		Text(
			text = value,
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.weight(0.7f),
		)
	}
}

/**
 * The ⋮ overflow menu used on song rows everywhere (Songs, Favorites, album/artist/playlist
 * detail). Play/queue actions sit above a divider; share, details, and delete below it.
 * [extraItems] lets hosts append list-specific entries (e.g. "Remove from Playlist").
 */
@Composable
fun SongOverflowMenu(
	onPlayNext: (() -> Unit)? = null,
	onAddToQueue: (() -> Unit)? = null,
	onAddToPlaylist: (() -> Unit)? = null,
	onShare: (() -> Unit)? = null,
	onDetails: (() -> Unit)? = null,
	onDelete: (() -> Unit)? = null,
	extraItems: (@Composable () -> Unit)? = null,
	modifier: Modifier = Modifier,
) {
	var menuExpanded by remember { mutableStateOf(false) }

	Box(modifier = modifier) {
		IconButton(
			onClick = { menuExpanded = true },
			modifier = Modifier.size(40.dp),
		) {
			Icon(
				imageVector = Icons.Outlined.MoreVert,
				contentDescription = "More options",
				tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
				modifier = Modifier.size(18.dp),
			)
		}
		DropdownMenu(
			expanded = menuExpanded,
			onDismissRequest = { menuExpanded = false },
		) {
			if (onPlayNext != null) {
				DropdownMenuItem(
					text = { Text("Play Next", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = {
						Text(
							"▶+",
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					},
					onClick = {
						menuExpanded = false
						onPlayNext()
					},
				)
			}
			if (onAddToQueue != null) {
				DropdownMenuItem(
					text = { Text("Add to Queue", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = {
						Icon(
							Icons.AutoMirrored.Filled.PlaylistAdd,
							contentDescription = null,
							modifier = Modifier.size(20.dp),
							tint = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					},
					onClick = {
						menuExpanded = false
						onAddToQueue()
					},
				)
			}
			if (onAddToPlaylist != null) {
				DropdownMenuItem(
					text = { Text("Add to Playlist", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = {
						Icon(
							Icons.AutoMirrored.Outlined.QueueMusic,
							contentDescription = null,
							modifier = Modifier.size(20.dp),
							tint = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					},
					onClick = {
						menuExpanded = false
						onAddToPlaylist()
					},
				)
			}
			extraItems?.invoke()
			if (onShare != null || onDetails != null || onDelete != null) {
				HorizontalDivider(
					modifier = Modifier.padding(vertical = 4.dp),
					thickness = 0.5.dp,
					color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
				)
			}
			if (onShare != null) {
				DropdownMenuItem(
					text = { Text("Share", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = {
						Icon(
							Icons.Outlined.Share,
							contentDescription = null,
							modifier = Modifier.size(20.dp),
							tint = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					},
					onClick = {
						menuExpanded = false
						onShare()
					},
				)
			}
			if (onDetails != null) {
				DropdownMenuItem(
					text = { Text("Song Details", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = {
						Icon(
							Icons.Outlined.Info,
							contentDescription = null,
							modifier = Modifier.size(20.dp),
							tint = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					},
					onClick = {
						menuExpanded = false
						onDetails()
					},
				)
			}
			if (onDelete != null) {
				DropdownMenuItem(
					text = { Text("Delete", style = MaterialTheme.typography.bodyMedium) },
					leadingIcon = {
						Icon(
							Icons.Outlined.Delete,
							contentDescription = null,
							modifier = Modifier.size(20.dp),
							tint = MaterialTheme.colorScheme.error,
						)
					},
					onClick = {
						menuExpanded = false
						onDelete()
					},
				)
			}
		}
	}
}
