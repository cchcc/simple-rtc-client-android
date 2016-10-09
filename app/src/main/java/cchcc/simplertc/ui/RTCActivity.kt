package cchcc.simplertc.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cchcc.simplertc.R
import cchcc.simplertc.inject.DaggerRTCViewModelComponent
import cchcc.simplertc.inject.PerRTCActivity
import cchcc.simplertc.inject.RTCViewModelModule
import cchcc.simplertc.inject.RTCWebSocketComponent
import cchcc.simplertc.viewmodel.RTCViewModel
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@PerRTCActivity
class RTCActivity : AppCompatActivity() {
    @Inject lateinit var viewModel: RTCViewModel
    private var messageSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val roomName = intent.getStringExtra("roomName")

        DaggerRTCViewModelComponent.builder()
            .rTCWebSocketComponent(rtcComponents.remove(roomName))
            .rTCViewModelModule(RTCViewModelModule(roomName))
            .build()
            .inject(this)

        setContentView(R.layout.act_rtc)

        // bind view model event
        messageSubscription = viewModel.messageObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({

        }, {

        }, {

        })



        viewModel.onCreate()
    }

    override fun onDestroy() {
        messageSubscription?.unsubscribe()
        messageSubscription = null
        viewModel.onDestroy()
        super.onDestroy()
    }

    companion object {
        val rtcComponents: MutableMap<String/*room name*/, RTCWebSocketComponent> by lazy {
            mutableMapOf<String, RTCWebSocketComponent>() }
    }
}
