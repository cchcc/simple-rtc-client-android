package cchcc.simplertc.ext

import cchcc.simplertc.App
import cchcc.simplertc.model.SignalMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.salomonbrys.kodein.instance

fun SignalMessage.toJsonString(): String = App.kodein.instance<ObjectMapper>().writeValueAsString(this)

fun String.toSignalMessage(): SignalMessage? = App.kodein.instance<ObjectMapper>().let {
    try {
        val type = it.typeFactory.constructType(Map::class.java)
        SignalMessage.from(it.readValue(this, type))
    } catch (e: Exception) {
        null
    }
}