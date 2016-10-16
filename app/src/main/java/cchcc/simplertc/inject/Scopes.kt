package cchcc.simplertc.inject

import com.github.salomonbrys.kodein.Scope
import com.github.salomonbrys.kodein.ScopeRegistry
import java.util.*

object Scopes {

    object UserData {
        val perRoomUserData: WeakHashMap<String/*room name*/, ScopeRegistry> by lazy {
            WeakHashMap<String, ScopeRegistry>()
        }
    }

    val perRoomSocket: Scope<String/*room name*/> by lazy {
        object : Scope<String> {
            override fun getRegistry(context: String): ScopeRegistry
                    = UserData.perRoomUserData.getOrPut(context) { ScopeRegistry() }
        }
    }
}