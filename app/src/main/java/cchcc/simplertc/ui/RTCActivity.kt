package cchcc.simplertc.ui

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import cchcc.simplertc.R
import cchcc.simplertc.ext.simpleAlert
import cchcc.simplertc.ext.toast
import cchcc.simplertc.inject.DaggerRTCViewModelComponent
import cchcc.simplertc.inject.PerRTCActivity
import cchcc.simplertc.inject.RTCViewModelModule
import cchcc.simplertc.inject.RTCWebSocketComponent
import cchcc.simplertc.viewmodel.RTCViewModel
import kotlinx.android.synthetic.main.act_rtc.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@PerRTCActivity
class RTCActivity : BaseActivity() {
    @Inject lateinit var viewModel: RTCViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val roomName = intent.getStringExtra("roomName")

        DaggerRTCViewModelComponent.builder()
                .rTCWebSocketComponent(rtcComponents.remove(roomName))
                .rTCViewModelModule(RTCViewModelModule())
                .build()
                .inject(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.act_rtc)

        // bind event from ViewModel
        viewModel.eventObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (it) {
                        is RTCViewModel.Event.Connected -> toast("connected")
                        is RTCViewModel.Event.Chat -> {}
                    }
                }, error@{
                    simpleAlert(it.toString())
                }, terminated@{
                    toast("terminated")
                }).addToComposite()

        viewModel.onCreate(this, glv_video)
    }

    override fun onResume() {
        super.onResume()
        glv_video.onResume()
    }

    override fun onPause() {
        glv_video.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        viewModel.terminate()
        viewModel.onDestroy()
        super.onDestroy()
    }

    companion object {
        val rtcComponents: MutableMap<String/*room name*/, RTCWebSocketComponent> by lazy {
            mutableMapOf<String, RTCWebSocketComponent>()
        }
    }
}
