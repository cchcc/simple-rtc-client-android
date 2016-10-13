package cchcc.simplertc.inject

import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.viewmodel.RTCViewModel
import dagger.Module
import dagger.Provides

@Module
class RTCViewModelModule() {
    @Provides
    @PerRTCActivity
    fun provideRTCViewModel(rtcWebSocket: RTCWebSocket): RTCViewModel = RTCViewModel(rtcWebSocket)
}