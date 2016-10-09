package cchcc.simplertc.inject

import cchcc.simplertc.ui.RTCActivity
import dagger.Component

@PerRTCActivity
@Component(modules = arrayOf(RTCViewModelModule::class)
    ,dependencies = arrayOf(RTCWebSocketComponent::class))
interface RTCViewModelComponent {
    fun inject(activity: RTCActivity)
}