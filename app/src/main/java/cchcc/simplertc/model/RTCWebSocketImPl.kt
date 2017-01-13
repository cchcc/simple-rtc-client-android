package cchcc.simplertc.model

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.ws.WebSocket
import okhttp3.ws.WebSocketCall
import okhttp3.ws.WebSocketListener
import okio.Buffer
import rx.Observable
import rx.subjects.ReplaySubject
import java.io.IOException

class RTCWebSocketImpl(val objectMapper: ObjectMapper, okHttpClient: OkHttpClient, serverUrl: String,  roomName: String) : RTCWebSocket {

    private var webSocket: WebSocket? = null
    private var createWebSocketCall: ()->WebSocketCall = {
        WebSocketCall.create(okHttpClient, Request.Builder().url(serverUrl).build())
    }
    private val receiveSubject: ReplaySubject<SignalMessage> by lazy {
        ReplaySubject.create<SignalMessage>()
    }

    override val isConnected: Boolean
        get() = webSocket != null

    override val messageObservable: Observable<SignalMessage> by lazy {
        receiveSubject.doOnSubscribe {
            if (webSocket == null)
                createWebSocketCall.invoke().enqueue(webSocketListener)
        }
    }

    private val webSocketListener: WebSocketListener by lazy {
        object : WebSocketListener {
            override fun onOpen(webSocket: WebSocket, response: Response?) {
                this@RTCWebSocketImpl.webSocket = webSocket
                send(SignalMessage.room(roomName))
            }

            override fun onPong(payload: Buffer?) {
            }

            override fun onClose(code: Int, reason: String?) {
                webSocket = null
                receiveSubject.onCompleted()
            }

            override fun onFailure(e: IOException?, response: Response?) {
                receiveSubject.onError(e)
            }

            override fun onMessage(resBody: ResponseBody?) {
                val msgString = resBody?.string()
                val msg = msgString?.toSignalMessage()
                if (msg != null)
                    receiveSubject.onNext(msg)
                else {
                    close()
                    receiveSubject.onError(Exception("unexpected type : $msgString"))
                }
            }
        }
    }

    override fun send(message: SignalMessage) {
        webSocket?.sendMessage(RequestBody.create(WebSocket.TEXT, message.toJsonString()))
    }

    override fun close() {
        webSocket?.close(1000, "bye")
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