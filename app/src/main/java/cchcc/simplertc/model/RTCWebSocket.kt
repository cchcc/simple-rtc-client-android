package cchcc.simplertc.model

import rx.Observable

interface RTCWebSocket {

    val isConnected: Boolean
    val messageObservable: Observable<SignalMessage>

    fun send(message: SignalMessage)
    fun close()
}