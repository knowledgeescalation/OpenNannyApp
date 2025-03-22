package com.example.opennannyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.opennannyapp.ui.theme.OpenNannyAppTheme
import com.example.opennannyapp.ui.viewModels.DirState
import com.example.opennannyapp.ui.viewModels.DirViewItem
import com.example.opennannyapp.ui.viewModels.Mp3State
import com.example.opennannyapp.ui.viewModels.Mp3ViewItem
import com.example.opennannyapp.ui.viewModels.MusicViewModel
import kotlinx.coroutines.delay

import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Slider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import com.example.opennannyapp.ui.viewModels.SongState


class MusicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as OpenNannyApp

        setContent {
            OpenNannyAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    DirectoryScreen(MusicViewModel(app.serviceApi))

                }
            }
        }
    }
}

@Composable
fun DirectoryScreen(viewModel: MusicViewModel) {
    val dirState = viewModel.dirState.collectAsState()
    val mp3State = viewModel.mp3State.collectAsState()
    val songState = viewModel.songState.collectAsState()

    var selectedDirectory by remember { mutableStateOf<String?>(null) }
    var selectedSong by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        viewModel.updateSongStatus()
    }



    // Rest of DirectoryScreen implementation
    when {
        // Display MediaPlayerScreen if song is playing or paused
        songState.value is SongState.Playing -> {
            MediaPlayerScreen(
                directory = (songState.value as SongState.Playing).directory,
                songName = (songState.value as SongState.Playing).songName,
                onStop = { selectedSong = null },
                viewModel = viewModel
            )
        }
        selectedSong != null -> {
            MediaPlayerScreen(
                directory = selectedDirectory!!,
                songName = selectedSong!!,
                onStop = { selectedSong = null },
                viewModel = viewModel
            )
        }
        selectedDirectory != null -> {
            SongsScreen(
                directoryName = selectedDirectory,
                mp3State = mp3State.value,
                onSongSelected = { selectedSong = it },
                onBackPressed = { selectedDirectory = null },
                loadSongs = { viewModel.getMp3(selectedDirectory!!) }
            )
        }
        else -> {
            DirectoriesScreen(
                dirState = dirState.value,
                onDirectorySelected = { selectedDirectory = it }
            )
        }
    }
}

@Composable
fun DirectoriesScreen(
    dirState: DirState,
    onDirectorySelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Music",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(40.dp))

            when (dirState) {
                is DirState.Error -> {
                    ErrorMessage(message = dirState.message)
                }
                is DirState.Success -> {
                    DirectoryList(
                        directories = dirState.list,
                        onDirectorySelected = onDirectorySelected
                    )
                }
                is DirState.Loading -> {
                    LoadingIndicator()
                }
            }
        }
    }
}

@Composable
fun SongsScreen(
    directoryName: String?,
    mp3State: Mp3State,
    onSongSelected: (String) -> Unit,
    onBackPressed: () -> Unit,
    loadSongs: () -> Unit
) {
    // Load songs when this composable is first displayed
    LaunchedEffect(directoryName) {
        loadSongs()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Songs in $directoryName",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (mp3State) {
            is Mp3State.Error -> {
                ErrorMessage(message = mp3State.message)
            }
            is Mp3State.Success -> {
                if (mp3State.list.isEmpty()) {
                    EmptyStateMessage(message = "No songs found in this directory")
                } else {
                    SongList(
                        songs = mp3State.list,
                        onSongSelected = onSongSelected
                    )
                }
            }
            is Mp3State.Loading -> {
                LoadingIndicator()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        BackButton(onBackPressed = onBackPressed)
    }
}

@Composable
fun DirectoryList(
    directories: List<DirViewItem>,
    onDirectorySelected: (String) -> Unit
) {
    if (directories.isEmpty()) {
        EmptyStateMessage(message = "No directories found")
    } else {
        LazyColumn {
            items(directories) { directory ->
                DirectoryItem(
                    directory = directory,
                    onClick = onDirectorySelected
                )
            }
        }
    }
}

@Composable
fun SongList(
    songs: List<Mp3ViewItem>,
    onSongSelected: (String) -> Unit
) {
    LazyColumn {
        items(songs) { song ->
            SongItem(
                song = song,
                onClick = onSongSelected
            )
        }
    }
}

@Composable
fun DirectoryItem(directory: DirViewItem, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(directory.dir) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = directory.dir,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun SongItem(song: Mp3ViewItem, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(song.file) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Song",
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = song.file,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun BackButton(onBackPressed: () -> Unit) {
    Button(
        onClick = onBackPressed,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back")
        }
    }
}


@Composable
fun MediaPlayerScreen(
    directory: String,
    songName: String,
    onStop: () -> Unit,
    viewModel: MusicViewModel
) {
    val songState by viewModel.songState.collectAsState()

    var volume by remember { mutableFloatStateOf(0.0f) }
    var progress by remember { mutableFloatStateOf(0.0f) }
    var progressTxt by remember { mutableLongStateOf(0.toLong()) }

    // Start playback when screen is shown if not already playing
    LaunchedEffect(songName) {
        if (songState !is SongState.Playing) {
            viewModel.playSong(songName=songName, directory = directory)
        }

        while (true) {
            delay(1000)
            if (songState is SongState.Playing) {
                if (!(songState as SongState.Playing).isPaused) {
                    val myDuration = (songState as SongState.Playing).duration
                    if (progressTxt < myDuration) {
                        progressTxt += 1
                        progress = progressTxt.toFloat() / myDuration.toFloat()
                    }
                }
            }

        }

    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Now Playing",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display current song name
        val displayName = if (songState is SongState.Playing) {
            (songState as SongState.Playing).songName
        } else {
            songName
        }

        Text(
            text = displayName,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (songState) {
            is SongState.Loading -> {
                CircularProgressIndicator()
            }

            is SongState.Error -> {
                val errorState = songState as SongState.Error
                ErrorMessage(errorState.message)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { viewModel.playSong(songName=songName, directory = directory) }) {
                    Text("Retry")
                }
            }

            is SongState.Stopped -> {
                Text("Playback stopped")

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { viewModel.playSong(songName=songName, directory = directory) }) {
                    Text("Play")
                }
            }

            is SongState.Playing -> {
                val playingState = songState as SongState.Playing

                if (playingState.currentProgress == 0.toLong()) {
                    progress = 0.0f
                    progressTxt = 0
                    playingState.currentProgress = -1
                }
                else {
                    if (playingState.currentProgress > 0) {
                        progress = playingState.currentProgress.toFloat() / playingState.duration.toFloat()
                        progressTxt = playingState.currentProgress
                        playingState.currentProgress = -1
                    }
                }


                // Progress bar that allows seeking
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Slider(
                        value = progress,
                        onValueChange =  { newProgress ->
                            // Just update local UI state
                            progress = newProgress
                            progressTxt = (playingState.duration * progress).toLong()
                        },
                        onValueChangeFinished = { viewModel.rewindSong(progressTxt) },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(progressTxt))
                        Text(formatTime(playingState.duration))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop button
                    IconButton(onClick = {
                        viewModel.stopSong()
                        onStop()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Stop, // Using MusicNote as Stop icon substitute
                            contentDescription = "Stop",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Play/Pause button
                    IconButton(
                        onClick = {
                            if (playingState.isPaused) {
                                viewModel.resumeSong()
                            } else {
                                viewModel.pauseSong()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (playingState.isPaused)
                                Icons.Default.PlayArrow else Icons.Default.Pause, // Using Info as Pause icon substitute
                            contentDescription = if (playingState.isPaused) "Resume" else "Pause",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Volume control
                Text(
                    text = "Volume",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Min")

                    if(playingState.volume != 2.0F) {
                        volume = playingState.volume
                        playingState.volume = 2.0F
                    }

                    Slider(
                        value = volume,
                        onValueChange =  { newVolume ->
                            // Just update local UI state
                            volume = newVolume
                        },
                       onValueChangeFinished = { viewModel.setVolume(volume) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Text("Max")
                }
            }

            else -> {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

    }
}

// Helper function to format time in MM:SS format
private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
