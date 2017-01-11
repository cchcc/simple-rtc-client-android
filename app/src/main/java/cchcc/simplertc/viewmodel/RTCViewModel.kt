package cchcc.simplertc.viewmodel

import cchcc.simplertc.model.ChatMessage
import com.github.salomonbrys.kodein.Kodein
import rx.Observable

interface RTCViewModel : LifeCycle {

    sealed class Event {
        class Connected : Event()
        class Chat(val message: ChatMessage) : Event()
    }

    val eventObservable: Observable<Event>

    override fun onCreate(kodein: Kodein)
    override fun onDestroy()

    fun sendChatMessage(msg: String)
    fun terminate()
}