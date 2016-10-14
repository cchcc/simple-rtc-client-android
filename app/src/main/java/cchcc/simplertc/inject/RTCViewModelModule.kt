package cchcc.simplertc.inject

import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.viewmodel.RTCViewModel
import cchcc.simplertc.viewmodel.RTCViewModelImpl
import dagger.Module
import dagger.Provides

@Module
class RTCViewModelModule() {
    @Provides
    @PerRTCActivity
    fun provideRTCViewModel(rtcWebSocket: RTCWebSocket): RTCViewModel = RTCViewModelImpl(rtcWebSocket)
}