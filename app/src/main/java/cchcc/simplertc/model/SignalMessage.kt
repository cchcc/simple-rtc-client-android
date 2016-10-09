package cchcc.simplertc.model

sealed class SignalMessage(val type: String) {

    class room(val name: String) : SignalMessage("room")

    class roomCreated(val name: String) : SignalMessage("roomCreated")

    class roomJoined(val name: String) : SignalMessage("roomJoined")

    class roomIsFull(val name: String) : SignalMessage("roomIsFull")

    class startAsCaller(val ice: List<cchcc.simplertc.model.ICEServer>) : SignalMessage("startAsCaller")

    class startAsCallee(val ice: List<cchcc.simplertc.model.ICEServer>) : SignalMessage("startAsCallee")

    class chat(val message: String) : SignalMessage("chat")

    class rtcOffer() : SignalMessage("rtcOffer")

    class rtcAnswer() : SignalMessage("rtcAnswer")

    class rtcCandidate() : SignalMessage("rtcCandidate")

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun from(map: Map<String, Any?>): SignalMessage? = try {
            val type = map["type"] as String

            when (type) {
                "room" -> room(map["name"] as String)
                "roomCreated" -> roomCreated(map["name"] as String)
                "roomJoined" -> roomJoined(map["name"] as String)
                "roomIsFull" -> roomIsFull(map["name"] as String)
                "startAsCaller" -> startAsCaller((map["ice"] as List<Map<String, String>>).map(::ICEServer))
                "startAsCallee" -> startAsCallee((map["ice"] as List<Map<String, String>>).map(::ICEServer))
                "chat" -> chat(map["message"] as String)
                "rtcOffer" -> rtcOffer()
                "rtcAnswer" -> rtcAnswer()
                "rtcCandidate" -> rtcCandidate()
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}