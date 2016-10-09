package cchcc.simplertc.inject

import cchcc.simplertc.model.RTCWebSocket
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient

@Module
class RTCWebSocketModule(val serverAddr: String, val roomName: String) {

    @Provides
    @PerConnection
    fun provideRTCWebSocket(okHttpClient: OkHttpClient): RTCWebSocket
            = RTCWebSocket(serverAddr, okHttpClient, roomName)
}