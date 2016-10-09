package cchcc.simplertc.model

import android.util.Log
import cchcc.simplertc.ext.toJsonString
import cchcc.simplertc.ext.toSignalMessage
import cchcc.simplertc.inject.PerConnection
import okhttp3.*
import okhttp3.ws.WebSocket
import okhttp3.ws.WebSocketCall
import okhttp3.ws.WebSocketListener
import okio.Buffer
import rx.Observable
import rx.subjects.BehaviorSubject
import java.io.IOException
import javax.inject.Inject

@PerConnection
class RTCWebSocket {

    var roomName: String? = null
    private var webSocket: WebSocket? = null
    private var createWebSocketCall: (()->WebSocketCall)? = null
    private val receiveSubject: BehaviorSubject<SignalMessage> by lazy {
        BehaviorSubject.create<SignalMessage>()
    }

    val observable: Observable<SignalMessage> by lazy {
        receiveSubject.doOnSubscribe { createWebSocketCall!!.invoke().enqueue(webSocketListener) }
    }

    private val webSocketListener: WebSocketListener by lazy {
        object : WebSocketListener {
            override fun onOpen(webSocket: WebSocket, response: Response?) {
                this@RTCWebSocket.webSocket = webSocket
                send(SignalMessage.room(roomName!!))
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
                Log.i("WebSocketListener", "onMessage : $msgString")
                val msg = msgString?.toSignalMessage()
                if (msg != null)
                    receiveSubject.onNext(msg)
                else {
                    close()
                    receiveSubject.onError(IllegalStateException("unexpected type : $msgString"))
                }
            }
        }
    }

    @Inject constructor(serverUrl: String, okHttpClient: OkHttpClient, roomName: String) {
        this.roomName = roomName

        createWebSocketCall = {
            WebSocketCall.create(okHttpClient, Request.Builder().url(serverUrl).build())
        }
    }

    fun send(message: SignalMessage) {
        webSocket?.sendMessage(RequestBody.create(WebSocket.TEXT, message.toJsonString()))
        Log.i("RTCWebSocket", "send : ${message.toJsonString()}")
    }

    fun close() {
        webSocket?.close(1000, "bye")
        webSocket = null
    }
}