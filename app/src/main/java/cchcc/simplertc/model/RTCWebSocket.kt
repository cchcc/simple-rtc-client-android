package cchcc.simplertc.model

import cchcc.simplertc.inject.PerConnection
import rx.Observable

@PerConnection
interface RTCWebSocket {

    val isConnected: Boolean
    val observable: Observable<SignalMessage>

    fun send(message: SignalMessage)
    fun close()
}