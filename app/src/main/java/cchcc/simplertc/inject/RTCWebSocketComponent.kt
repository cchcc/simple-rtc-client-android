package cchcc.simplertc.inject

import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.viewmodel.RTCViewModel
import dagger.Component

@PerConnection
@Component(dependencies = arrayOf(AppComponent::class)
        , modules = arrayOf(RTCWebSocketModule::class))
interface RTCWebSocketComponent {
    fun inject(viewModel: RTCViewModel)
    fun rtcWebSocket(): RTCWebSocket
}