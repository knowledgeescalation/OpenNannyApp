package com.example.opennannyapp

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.opennannyapp.api.WebRTC
import com.example.opennannyapp.ui.theme.OpenNannyAppTheme
import com.example.opennannyapp.ui.viewModels.LedState
import com.example.opennannyapp.ui.viewModels.LedViewModel
import io.getstream.webrtc.android.ui.VideoTextureViewRenderer
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack

class VideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as OpenNannyApp

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL  // Not voice call mode
        audioManager.isSpeakerphoneOn = true

        setContent {
            OpenNannyAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    VideoScreen(WebRTC(context = this, app.networkModule, app.api_ip), LedViewModel(app.serviceApi))
                }

            }
        }
    }
}

@Composable
fun VideoScreen(webRTCsession: WebRTC, viewModel: LedViewModel) {

    var sliderPosition by remember { mutableStateOf(0f) }
    var isOn by remember { mutableStateOf(false) }

    val initState = viewModel.initState.collectAsState()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        webRTCsession.onSessionScreenReady()
    }

    DisposableEffect(Unit) {
        onDispose {
            webRTCsession.destroy()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val remoteVideoTrackState by webRTCsession.remoteVideoTrackFlow.collectAsStateWithLifecycle(null)
        val remoteVideoTrack = remoteVideoTrackState

        val remoteAudioTrackState by webRTCsession.remoteAudioTrackFlow.collectAsStateWithLifecycle(null)
        val remoteAudioTrack = remoteAudioTrackState

        if (remoteAudioTrack != null) {

            AudioRenderer(
                audioTrack = remoteAudioTrack
            )
        }


        if (remoteVideoTrack != null) {

            VideoRenderer(
                eglContext = webRTCsession.eglBaseContext,
                videoTrack = remoteVideoTrack,
                modifier = Modifier
                    .align(Alignment.Center)
            )

        }
        else {
            Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier.align(Alignment.Center)) {
                CircularProgressIndicator()
            }

        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {

            when(val stateInitValue = initState.value) {
                is LedState.Error -> {
                    Text(text = stateInitValue.message, fontSize = 24.sp, color = Color.White)
                }

                is LedState.Success -> {

                    if(stateInitValue.status == "day") {
                        sliderPosition = 0f
                        isOn = false
                    }

                    if(stateInitValue.status == "night") {
                        sliderPosition = 1f
                        isOn = true
                    }

                    when(val stateValue = state.value) {
                        is LedState.Error -> {
                            Text(text = stateValue.message, fontSize = 24.sp, color = Color.White)
                        }

                        is LedState.Success -> {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.sun),
                                    contentDescription = "sun",
                                    modifier = Modifier.size(24.dp) // Adjust icon size
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = sliderPosition,
                                    onValueChange = {
                                        viewModel.stopInit()
                                        sliderPosition = it
                                    },
                                    onValueChangeFinished = {
                                        isOn = sliderPosition > 0.5f
                                        sliderPosition = if (isOn) 1f else 0f

                                        if(stateInitValue.status == "") {
                                            if(isOn) {
                                                viewModel.sendCmd("night")
                                            }
                                            else {
                                                viewModel.sendCmd("day")
                                            }
                                        }
                                    },
                                    valueRange = 0f..1f,
                                    modifier = Modifier.width(250.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.moon),
                                    contentDescription = "sun",
                                    modifier = Modifier.size(24.dp) // Adjust icon size
                                )

                            }

                        }

                        LedState.Loading -> {
                            CircularProgressIndicator()
                        }
                    }

                }

                LedState.Loading -> {
                    CircularProgressIndicator()
                }
            }

        }
    }
}

@Composable
fun VideoRenderer(
    eglContext: EglBase.Context,
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    val trackState: MutableState<VideoTrack?> = remember { mutableStateOf(null) }
    var view: VideoTextureViewRenderer? by remember { mutableStateOf(null) }

    DisposableEffect(videoTrack) {
        onDispose {
            cleanTrack(view, trackState)
        }
    }

    AndroidView(
        factory = { context ->
            VideoTextureViewRenderer(context).apply {
                init(
                    eglContext,
                    object : RendererCommon.RendererEvents {
                        override fun onFirstFrameRendered() = Unit

                        override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) = Unit
                    }
                )
                setupVideo(trackState, videoTrack, this)
                view = this
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            }
        },
        update = { v -> setupVideo(trackState, videoTrack, v) },
        modifier = modifier
    )
}

private fun cleanTrack(
    view: VideoTextureViewRenderer?,
    trackState: MutableState<VideoTrack?>
) {
    view?.let { trackState.value?.removeSink(it) }
    trackState.value = null
}

private fun setupVideo(
    trackState: MutableState<VideoTrack?>,
    track: VideoTrack,
    renderer: VideoTextureViewRenderer
) {
    if (trackState.value == track) {
        return
    }

    cleanTrack(renderer, trackState)

    trackState.value = track
    track.addSink(renderer)
}

@Composable
fun AudioRenderer(
    audioTrack: AudioTrack
) {
    DisposableEffect(audioTrack) {
        // Enable the audio track for playback
        audioTrack.setEnabled(true)

        onDispose {
            // Disable the track when component is disposed
            audioTrack.setEnabled(false)
        }
    }
}

@Composable
fun AudioVolumeControl(
    audioTrack: AudioTrack,
    modifier: Modifier = Modifier
) {
    // State to track if the audio is muted
    var isMuted by remember { mutableStateOf(false) }

    // Current volume level (this will only control mute/unmute)
    var volume by remember { mutableStateOf(1f) }

    // Remember the initial enabled state
    val initialEnabledState = remember { audioTrack.enabled() }

    // Apply mute state to the track whenever it changes
    LaunchedEffect(isMuted) {
        audioTrack.setEnabled(!isMuted)
    }

    // Restore original state when component is disposed
    DisposableEffect(audioTrack) {
        onDispose {
            audioTrack.setEnabled(initialEnabledState)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Label
        Text(
            text = "Audio:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )

        // Volume slider (only controls mute/unmute in reality)
        Slider(
            value = volume,
            onValueChange = {
                volume = it
                // Update mute state based on slider position
                isMuted = volume <= 0.01f
                audioTrack.setEnabled(!isMuted)
            },
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Display volume percentage or mute status
        Text(
            text = if (isMuted) "Muted" else "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}