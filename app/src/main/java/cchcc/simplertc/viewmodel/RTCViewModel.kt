package cchcc.simplertc.viewmodel

import android.content.Context
import android.opengl.GLSurfaceView
import cchcc.simplertc.inject.PerRTCActivity
import cchcc.simplertc.model.ChatMessage
import rx.Observable

@PerRTCActivity
interface RTCViewModel {

    sealed class Event {
        class Connected() : Event()
        class Chat(val message: ChatMessage) : Event()
    }

    val eventObservable: Observable<Event>

    fun onCreate(context: Context, glv_video: GLSurfaceView)

    fun onDestroy()
    fun sendChatMessage(msg: String)

    fun terminate()

}