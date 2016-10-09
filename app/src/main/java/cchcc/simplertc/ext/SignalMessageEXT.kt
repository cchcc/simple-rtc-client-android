package cchcc.simplertc.ext

import cchcc.simplertc.App
import cchcc.simplertc.model.SignalMessage

fun SignalMessage.toJsonString(): String = App.component.jsonMapper().writeValueAsString(this)

fun String.toSignalMessage(): SignalMessage? = App.component.jsonMapper().let {
    try {
        val type = it.typeFactory.constructType(Map::class.java)
        SignalMessage.from(it.readValue(this, type))
    } catch (e: Exception) {
        null
    }
}