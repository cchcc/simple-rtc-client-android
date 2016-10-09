package cchcc.simplertc.viewmodel

import cchcc.simplertc.inject.PerRTCActivity
import cchcc.simplertc.model.ICEServer
import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.model.SignalMessage
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject
import javax.inject.Inject

@PerRTCActivity
class RTCViewModel {

    private val rtcWebSocket: RTCWebSocket
    private val roomName: String
    private var rtcWebSocketSubscription: Subscription? = null
    private val messageSubject: PublishSubject<String> by lazy {
        PublishSubject.create<String>()
    }

    val messageObservable: Observable<String> by lazy {
        messageSubject.asObservable()
    }

    @Inject constructor(roomName: String, rtcWebSocket: RTCWebSocket) {
        this.roomName = roomName
        this.rtcWebSocket = rtcWebSocket
    }

    fun onCreate() {
        // bind model event
        rtcWebSocketSubscription = rtcWebSocket.observable.subscribe(
                {
                    when (it) {
                        is SignalMessage.startAsCaller -> startRTC(true, it.ice)
                        is SignalMessage.startAsCallee -> startRTC(false, it.ice)
                        is SignalMessage.chat -> messageSubject.onNext(it.message)
                        is SignalMessage.rtcOffer -> {}
                        is SignalMessage.rtcAnswer -> {}
                        is SignalMessage.rtcCandidate -> {}
                    }
                }
                , { messageSubject.onError(it) }
                , { messageSubject.onCompleted() })
    }

    fun onDestroy() {
        rtcWebSocketSubscription?.unsubscribe()
        rtcWebSocketSubscription = null
        rtcWebSocket.close()
    }

    fun sendChatMessage(msg: String): Unit = rtcWebSocket.send(SignalMessage.chat(msg))

    private fun startRTC(isCaller: Boolean, iceList: List<ICEServer>) {

    }

}