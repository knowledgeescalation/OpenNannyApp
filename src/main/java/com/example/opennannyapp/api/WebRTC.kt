package com.example.opennannyapp.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

class WebRTC(private val context: Context, private val networkModule: NetworkModule, private val api_ip: String) {

    private var offer: String? = null

    private val _remoteVideoTrackFlow = MutableSharedFlow<VideoTrack>()
    val remoteVideoTrackFlow: SharedFlow<VideoTrack> = _remoteVideoTrackFlow

    private val _remoteAudioTrackFlow = MutableSharedFlow<AudioTrack>()
    val remoteAudioTrackFlow: SharedFlow<AudioTrack> = _remoteAudioTrackFlow

    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val client = OkHttpClient()
    private val request = Request
        .Builder()
        .url("wss://$api_ip/webrtc")
        .header("Authorization", "Bearer ${networkModule.getToken()}")
        .build()

    private var ws = client.newWebSocket(request, SignalingWebSocketListener())

    val eglBaseContext: EglBase.Context by lazy {
        EglBase.create().eglBaseContext
    }

    private val factory by lazy {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )

        PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .createPeerConnectionFactory()
    }

    private val peerConnection = factory.createPeerConnection(
        emptyList<PeerConnection.IceServer>(),
        object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onDataChannel(dataChannel: DataChannel) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
            override fun onAddStream(mediaStream: MediaStream) {}
            override fun onRemoveStream(mediaStream: MediaStream) {}
            override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
            override fun onRenegotiationNeeded() {}
            override fun onTrack(transceiver: RtpTransceiver) {
                val track = transceiver.receiver.track()
                if (track is VideoTrack) {
                    //sessionManagerScope.launch(Dispatchers.Main) {
                    sessionManagerScope.launch {
                        _remoteVideoTrackFlow.emit(track)
                    }

                } else if (track is AudioTrack) {
                    sessionManagerScope.launch {
                        _remoteAudioTrackFlow.emit(track)
                    }
                }
            }
        }
    ) ?: throw IllegalStateException("Failed to create PeerConnection")


    private fun sendAnswer() {

        val json = offer?.let { JSONObject(it) }
        if (json != null) {
            if (json.has("sdp")) {
                val sdp = SessionDescription(
                    SessionDescription.Type.OFFER,
                    json.getString("sdp")
                )
                peerConnection.setRemoteDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        peerConnection.createAnswer(object : SdpObserver {
                            override fun onCreateSuccess(answer: SessionDescription?) {
                                peerConnection.setLocalDescription(this, answer)
                                ws.send(answer?.description.toString())
                            }

                            override fun onSetSuccess() {}
                            override fun onCreateFailure(error: String?) {}
                            override fun onSetFailure(error: String?) {}
                        }, MediaConstraints())
                    }

                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) {}
                    override fun onSetFailure(error: String?) {}
                }, sdp)
            }
        }

    }


    fun onSessionScreenReady() {
        sessionManagerScope.launch {


            while (offer == null) {
                delay(100)
            }

            sendAnswer()


        }
    }

    private inner class SignalingWebSocketListener : WebSocketListener() {

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            if (response?.code == 403) {

                CoroutineScope(Dispatchers.IO).launch {
                    delay(1000) // Delay for 1 seconds

                    reconnectWebSocket()
                }

            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {

            offer = text

        }
    }

    private fun reconnectWebSocket() {
        ws.cancel()
        val newRequest = request.newBuilder()
            .header("Authorization", "Bearer ${networkModule.getToken()}")
            .build()
        ws = client.newWebSocket(newRequest, SignalingWebSocketListener())
    }

    fun destroy() {
        // Close peer connection
        peerConnection.close()

        // Release EGL resources
        (eglBaseContext as? EglBase)?.release()

        ws.close(1000, "User requested close")
    }

}
