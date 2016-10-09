package cchcc.simplertc.inject

import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.viewmodel.RTCViewModel
import dagger.Module
import dagger.Provides

@Module
class RTCViewModelModule(val roonName: String) {
    @Provides
    @PerRTCActivity
    fun provideRTCViewModel(rtcWebSocket: RTCWebSocket): RTCViewModel
            = RTCViewModel(roonName, rtcWebSocket)
}