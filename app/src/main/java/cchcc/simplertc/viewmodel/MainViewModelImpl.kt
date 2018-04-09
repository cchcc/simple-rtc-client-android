package cchcc.simplertc.viewmodel

import cchcc.simplertc.ext.onNextAndCompleted
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import okhttp3.*
import okio.Buffer
import rx.Observable
import rx.lang.kotlin.observable
import java.io.IOException

class MainViewModelImpl : MainViewModel, KodeinInjected {
    override val injector = KodeinInjector()

    private val okHttpClient: OkHttpClient by instance()
    private val url: String by instance("serverAddress")

    override fun onCreate(kodein: Kodein) {
        injector.inject(kodein)
    }

    override fun checkServerIsOn(): Observable<Boolean> = observable { subscriber ->
        okHttpClient.newWebSocket(Request.Builder().url(url).build(), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response?) {
                webSocket.close(1000, null)
                subscriber.onNextAndCompleted(true)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                subscriber.onNextAndCompleted(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                subscriber.onNextAndCompleted(false)
            }
        })
    }
}