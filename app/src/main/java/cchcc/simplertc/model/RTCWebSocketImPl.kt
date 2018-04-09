package cchcc.simplertc.model

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okio.ByteString
import rx.Observable
import rx.subjects.ReplaySubject

class RTCWebSocketImpl(val objectMapper: ObjectMapper, okHttpClient: OkHttpClient, serverUrl: String,  roomName: String) : RTCWebSocket {

    private var webSocket: WebSocket? = null
    private val receiveSubject: ReplaySubject<SignalMessage> by lazy {
        ReplaySubject.create<SignalMessage>()
    }

    override val isConnected: Boolean
        get() = webSocket != null

    override val messageObservable: Observable<SignalMessage> by lazy {
        receiveSubject.doOnSubscribe {
            if (webSocket == null) {
                okHttpClient.newWebSocket(Request.Builder().url(serverUrl).build(), webSocketListener)
            }
        }
    }

    private val webSocketListener: WebSocketListener by lazy {
        object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@RTCWebSocketImpl.webSocket = webSocket
                send(SignalMessage.room(roomName))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val msg = text.toSignalMessage()
                if (msg != null)
                    receiveSubject.onNext(msg)
                else {
                    close()
                    receiveSubject.onError(Exception("unexpected type : $text"))
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {}

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@RTCWebSocketImpl.webSocket = null
                receiveSubject.onCompleted()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                receiveSubject.onError(t)
            }
        }
    }

    override fun send(message: SignalMessage) {
        webSocket?.send(message.toJsonString())
    }

    override fun close() {
        webSocket?.close(1000, null)
        webSocket = null
    }

    fun SignalMessage.toJsonString(): String = objectMapper.writeValueAsString(this)

    fun String.toSignalMessage(): SignalMessage? = objectMapper.let {
        try {
            val type = it.typeFactory.constructType(Map::class.java)
            SignalMessage.from(it.readValue(this, type))
        } catch (e: Exception) {
            null
        }
    }
}