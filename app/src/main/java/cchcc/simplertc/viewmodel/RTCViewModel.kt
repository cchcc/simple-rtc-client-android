package cchcc.simplertc.viewmodel

import android.content.Context
import android.opengl.GLSurfaceView
import cchcc.simplertc.inject.PerRTCActivity
import cchcc.simplertc.model.ICEServer
import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.model.SignalMessage
import org.webrtc.*
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject
import java.util.regex.Pattern
import javax.inject.Inject

@PerRTCActivity
class RTCViewModel {

    sealed class Event {
        class Connected() : Event()
        class Chat(val message: String) : Event()
    }

    private val rtcWebSocket: RTCWebSocket
    private lateinit var rtcWebSocketSubscription: Subscription
    private val eventSubject: PublishSubject<Event> by lazy {
        PublishSubject.create<Event>()
    }

    val eventObservable: Observable<Event> by lazy {
        eventSubject.asObservable()
    }

    val isConnected: Boolean
        get() = rtcWebSocket.isConnected && peerConnection != null

    private var isCaller = true
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var localMediaStream: MediaStream
    private var videoSource: VideoSource? = null
    private var peerConnection: PeerConnection? = null
    private var remoteRender: VideoRenderer.Callbacks? = null
    private var localRender: VideoRenderer.Callbacks? = null

    @Inject constructor(rtcWebSocket: RTCWebSocket) {
        this.rtcWebSocket = rtcWebSocket
    }




    fun onCreate(context: Context, glv_video: GLSurfaceView) {
        PeerConnectionFactory.initializeAndroidGlobals(context, true, true, false)
        VideoRendererGui.setView(glv_video) {}

        peerConnectionFactory = PeerConnectionFactory()

        localMediaStream = peerConnectionFactory.createLocalMediaStream("localMediaStream")

        // add video track
        val videoCapture = createVideoCapturer() ?: run {
            eventSubject.onError(RuntimeException("Failed to open camera"))
            return
        }

        videoSource = peerConnectionFactory.createVideoSource(videoCapture, MediaConstraints())
        val videoTrack = peerConnectionFactory.createVideoTrack("videoTrack", videoSource)

        remoteRender = VideoRendererGui.createGuiRenderer(0, 0, 100, 100
                , RendererCommon.ScalingType.SCALE_ASPECT_FILL, false)
        localRender = VideoRendererGui.createGuiRenderer(0, 0, 100, 100
                , RendererCommon.ScalingType.SCALE_ASPECT_FILL, true)

        videoTrack!!.addRenderer(VideoRenderer(localRender))
        localMediaStream.addTrack(videoTrack)

        // add audio track
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource)
        localMediaStream.addTrack(audioTrack)

        rtcWebSocketSubscription = rtcWebSocket.observable.subscribe({
            when (it) {
                is SignalMessage.startAsCaller -> startRTC(true, it.ice)
                is SignalMessage.startAsCallee -> startRTC(false, it.ice)
                is SignalMessage.chat -> eventSubject.onNext(Event.Chat(it.message))
                is SignalMessage.rtcOffer -> receivedRemoteSDP(it.sdpType, it.sdpDescription)
                is SignalMessage.rtcAnswer -> receivedRemoteSDP(it.sdpType, it.sdpDescription)
                is SignalMessage.rtcCandidate ->
                    peerConnection!!.addIceCandidate(IceCandidate(it.id, it.label, it.candidate))
            }
        }, failedToConnect@{
            eventSubject.onError(it)
        }, disconnected@{
            terminate()
            eventSubject.onCompleted()
        })
    }

    fun onDestroy() {
        videoSource?.stop()
        peerConnectionFactory.dispose()
    }

    fun sendChatMessage(msg: String): Unit = send(SignalMessage.chat(msg))

    fun terminate() {
        if (!rtcWebSocketSubscription.isUnsubscribed)
            rtcWebSocketSubscription.unsubscribe()

        if (rtcWebSocket.isConnected)
            rtcWebSocket.close()

        peerConnection?.dispose()
        peerConnection = null
    }

    private fun send(signalMessage: SignalMessage) = rtcWebSocket.send(signalMessage)

    private fun startRTC(isCaller: Boolean, iceList: List<ICEServer>) {
        this.isCaller = isCaller

        val iceListToParam = iceList.map {
            PeerConnection.IceServer(it.uri, it.username, it.password)
        }.toList()

        val pcConfig = PeerConnection.RTCConfiguration(iceListToParam).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
        }

        val peerConnectionConstraints = MediaConstraints().apply {
            optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
            optional.add(MediaConstraints.KeyValuePair("googImprovedWifiBwe", "true"))
        }

        peerConnection = peerConnectionFactory.createPeerConnection(pcConfig
                , peerConnectionConstraints, peerConnectionObserver).apply {
            addStream(localMediaStream)
        }

        if (isCaller) {
            val sdpConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            }

            peerConnection!!.createOffer(sdpObserver, sdpConstraints)
        }
    }

    private fun receivedRemoteSDP(type: String, sdpDescription: String) {
        val newSdp = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true)
        peerConnection!!.setRemoteDescription(sdpObserver
                , SessionDescription(SessionDescription.Type.fromCanonicalForm(type), newSdp))
    }

    private val peerConnectionObserver: PeerConnection.Observer by lazy {
        object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                send(SignalMessage.rtcCandidate(candidate.sdpMLineIndex, candidate.sdpMid, candidate.sdp))
            }

            override fun onDataChannel(dataChannel: DataChannel?) {
            }

            override fun onIceConnectionReceivingChange(changed: Boolean) {
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        VideoRendererGui.update(localRender, 8, 73, 25, 22
                                , RendererCommon.ScalingType.SCALE_ASPECT_FIT, true)
                        VideoRendererGui.update(remoteRender, 0, 0, 100, 100
                                , RendererCommon.ScalingType.SCALE_ASPECT_FILL, false)
                        eventSubject.onNext(Event.Connected())
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED -> {
                        terminate()
                        eventSubject.onCompleted()
                    }
                    else -> {
                    }
                }
            }

            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
            }

            override fun onAddStream(mediaStream: MediaStream) {
                if (mediaStream.videoTracks.size == 1) {
                    val remoteVideoTrack = mediaStream.videoTracks[0]
                    remoteVideoTrack.addRenderer(VideoRenderer(remoteRender))
                }
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
            }

            override fun onRemoveStream(mediaStream: MediaStream) {
                mediaStream.videoTracks[0].dispose()
            }

            override fun onRenegotiationNeeded() {
            }
        }
    }


    private val sdpObserver: SdpObserver by lazy {
        object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                val sdpDescription = preferCodec(sdp.description, AUDIO_CODEC_ISAC, true)
                val newSdp = SessionDescription(sdp.type, sdpDescription)
                peerConnection!!.setLocalDescription(sdpObserver, newSdp)
            }

            override fun onCreateFailure(reason: String?) {
                eventSubject.onError(Exception("sdp create failure : $reason"))
            }

            override fun onSetFailure(reason: String?) {
                eventSubject.onError(Exception("sdp set failure : $reason"))
            }

            override fun onSetSuccess() {
                if (isCaller) {
                    if (peerConnection!!.remoteDescription == null)
                        peerConnection!!.localDescription.let {
                            send(SignalMessage.rtcOffer(it.type.canonicalForm(), it.description))
                        }
                } else {
                    if (peerConnection!!.localDescription == null) {
                        val sdpConstraints = MediaConstraints().apply {
                            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                        }
                        peerConnection!!.createAnswer(sdpObserver, sdpConstraints)
                    } else {
                        peerConnection!!.localDescription.let {
                            send(SignalMessage.rtcAnswer(it.type.canonicalForm(), it.description))
                        }
                    }
                }
            }
        }
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val cameraFacing = arrayOf("front", "back")
        val cameraIndex = intArrayOf(0, 1)
        val cameraOrientation = intArrayOf(0, 90, 180, 270)
        for (facing in cameraFacing) {
            for (index in cameraIndex) {
                for (orientation in cameraOrientation) {
                    val name = "Camera $index, Facing $facing, Orientation $orientation"
                    val vc = VideoCapturer.create(name)
                    if (vc != null)
                        return vc
                }
            }
        }
        return null
    }

    private fun preferCodec(sdpDescription: String, codec: String, isAudio: Boolean): String {
        val lines = sdpDescription.split("\r\n".toRegex())
                .dropLastWhile(String::isEmpty).toTypedArray()
        var mLineIndex = -1
        var codecRtpMap: String? = null
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        val regex = "^a=rtpmap:(\\d+) $codec(/\\d+)+[\r]?$"
        val codecPattern = Pattern.compile(regex)
        var mediaDescription = "m=video "
        if (isAudio)
            mediaDescription = "m=audio "
        var i = 0
        while (i < lines.size && (mLineIndex == -1 || codecRtpMap == null)) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i
                i++
                continue
            }
            val codecMatcher = codecPattern.matcher(lines[i])
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1)
            }
            i++
        }
        if (mLineIndex == -1)
            return sdpDescription
        if (codecRtpMap == null)
            return sdpDescription
        val origMLineParts = lines[mLineIndex].split(" ".toRegex())
                .dropLastWhile(String::isEmpty).toTypedArray()
        if (origMLineParts.size > 3) {
            val newMLine = StringBuilder()
            var origPartIndex = 0
            // Format is: m=<media> <port> <proto> <fmt> ...
            newMLine.append(origMLineParts[origPartIndex++]).append(" ")
            newMLine.append(origMLineParts[origPartIndex++]).append(" ")
            newMLine.append(origMLineParts[origPartIndex++]).append(" ")
            newMLine.append(codecRtpMap)
            while (origPartIndex < origMLineParts.size) {
                if (origMLineParts[origPartIndex] != codecRtpMap) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex])
                }
                origPartIndex++
            }
            lines[mLineIndex] = newMLine.toString()
        }
        val newSdpDescription = StringBuilder()
        for (line in lines)
            newSdpDescription.append(line).append("\r\n")
        return newSdpDescription.toString()
    }

    companion object {
        val AUDIO_CODEC_ISAC = "ISAC"
    }


}