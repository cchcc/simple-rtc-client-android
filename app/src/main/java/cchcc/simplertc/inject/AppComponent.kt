package cchcc.simplertc.inject

import cchcc.simplertc.App
import cchcc.simplertc.model.RTCWebSocket
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Component
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, NetworkModule::class))
interface AppComponent {
    fun inject(app: App)
    fun inject(rtcWebSocket: RTCWebSocket)
    fun app(): App
    fun okHttpClient(): OkHttpClient
    fun jsonMapper(): ObjectMapper
}