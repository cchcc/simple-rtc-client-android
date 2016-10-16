package cchcc.simplertc.viewmodel

import android.content.Context
import android.opengl.GLSurfaceView
import cchcc.simplertc.model.ChatMessage
import rx.Observable

interface RTCViewModel {

    sealed class Event {
        class Connected() : Event()
        class Chat(val message: ChatMessage) : Event()
    }

    val observable: Observable<Event>

    fun onCreate(context: Context, glv_video: GLSurfaceView)

    fun onDestroy()

    fun sendChatMessage(msg: String)

    fun terminate()

}