package cchcc.simplertc.viewmodel

import cchcc.simplertc.ext.onNextAndCompleted
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ws.WebSocket
import okhttp3.ws.WebSocketCall
import okhttp3.ws.WebSocketListener
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
        WebSocketCall.create(okHttpClient, Request.Builder().url(url).build())
                .enqueue(object : WebSocketListener {
                    override fun onOpen(webSocket: WebSocket?, response: Response?) {
                        webSocket?.close(1000, "")
                        subscriber.onNextAndCompleted(true)
                    }

                    override fun onPong(payload: Buffer?) {
                    }

                    override fun onClose(code: Int, reason: String?) {
                        subscriber.onNextAndCompleted(false)
                    }

                    override fun onFailure(e: IOException?, response: Response?) {
                        subscriber.onNextAndCompleted(false)
                    }

                    override fun onMessage(message: ResponseBody?) {
                    }
                })
    }
}