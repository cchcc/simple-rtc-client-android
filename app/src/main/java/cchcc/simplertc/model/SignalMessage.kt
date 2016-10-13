package cchcc.simplertc.model

sealed class SignalMessage(val type: String) {

    class room(val name: String) : SignalMessage("room")

    class roomCreated(val name: String) : SignalMessage("roomCreated")

    class roomJoined(val name: String) : SignalMessage("roomJoined")

    class roomIsFull(val name: String) : SignalMessage("roomIsFull")

    class startAsCaller(val ice: List<cchcc.simplertc.model.ICEServer>) : SignalMessage("startAsCaller")

    class startAsCallee(val ice: List<cchcc.simplertc.model.ICEServer>) : SignalMessage("startAsCallee")

    class chat(val message: String) : SignalMessage("chat")

    class rtcOffer(val sdpType: String, val sdpDescription: String) : SignalMessage("rtcOffer")

    class rtcAnswer(val sdpType: String, val sdpDescription: String) : SignalMessage("rtcAnswer")

    class rtcCandidate(val label: Int, val id: String, val candidate: String) : SignalMessage("rtcCandidate")

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
                "rtcOffer" -> rtcOffer(map["sdpType"] as String, map["sdpDescription"] as String)
                "rtcAnswer" -> rtcAnswer(map["sdpType"] as String, map["sdpDescription"] as String)
                "rtcCandidate" -> rtcCandidate(map["label"] as Int, map["id"] as String, map["candidate"] as String)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}