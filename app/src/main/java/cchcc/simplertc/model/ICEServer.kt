package cchcc.simplertc.model

class ICEServer(map: Map<String, String>) {
    val uri: String by map
    val username: String by map
    val password: String by map

    companion object {
        fun create(uri: String, username: String, password: String): ICEServer
            = ICEServer(mapOf("uri" to uri
                , "username" to username
                , "password" to password
        ))
    }
}