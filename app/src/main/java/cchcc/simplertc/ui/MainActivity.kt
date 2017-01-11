package cchcc.simplertc.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import cchcc.simplertc.G
import cchcc.simplertc.R
import cchcc.simplertc.ext.*
import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.model.SignalMessage
import cchcc.simplertc.viewmodel.MainViewModel
import cchcc.simplertc.viewmodel.RTCViewModel
import cchcc.simplertc.viewmodel.RTCViewModelImpl
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.conf.ConfigurableKodein
import kotlinx.android.synthetic.main.act_main.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity(), KodeinInjected {

    override val injector = KodeinInjector()

    private val mainViewModel: MainViewModel by instance()
    private val createRTCWebSocket: (String) -> RTCWebSocket by factory()
    private var isDestroyedActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        injector.inject(appKodein().instance<Kodein>(MainActivity::class))

        mainViewModel.onCreate(Kodein { extend(appKodein()) })

        tv_desc.text = "signal server : ${G.SIGNAL_SERVER_ADDR}"
        with(et_room_name) {
            setOnEditorActionListener { textView, id, keyEvent ->
                if (id == EditorInfo.IME_ACTION_GO)
                    connectToServerWithRoomName()
                true
            }
            setText(preferences.getString(G.PFK_ROOM_NAME, ""))
            setSelection(text.length)
        }
        bt_go.setOnClickListener { connectToServerWithRoomName() }

        checkServerIsOn()
    }

    override fun onDestroy() {
        isDestroyedActivity = true
        super.onDestroy()
    }

    private fun checkServerIsOn() {
        mainViewModel.checkServerIsOn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    AnimationUtils.loadAnimation(this, R.anim.clockwise_rotation).apply {
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.RESTART
                    }.let { tv_server_status.startAnimation(it) }
                }
                .doOnTerminate {
                    if (!isDestroyedActivity)
                        tv_server_status.clearAnimation()
                }
                .subscribe {
                    if (it) {
                        tv_server_status.text = "ON"
                        tv_server_status.setTextColor(Color.GREEN)
                    } else {
                        tv_server_status.text = "OFF"
                        tv_server_status.setTextColor(Color.RED)
                    }
                }
    }

    private fun connectToServerWithRoomName() {
        val roomName = et_room_name.text.toString()
        if (roomName.isEmpty() or roomName.isBlank()) {
            et_room_name.startAnimationNope()
            return
        }

        checkOrRequestPermissions(Manifest.permission.CAMERA) {
            preferences.save { putString(G.PFK_ROOM_NAME, roomName) }

            val rtcWebSocket = createRTCWebSocket(roomName)
            var subscription: Subscription? = null
            subscription = rtcWebSocket.messageObservable
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { showLoading() }
                    .doOnNext { hideLoading() }
                    .doOnError { hideLoading() }
                    .subscribe({
                        when (it) {
                            is SignalMessage.roomCreated,
                            is SignalMessage.roomJoined -> {
                                subscription?.unsubscribe()

                                with(appKodein().instance<Kodein>(RTCActivity::class)
                                        as ConfigurableKodein) {
                                    clear()
                                    addExtend(appKodein())
                                    addConfig {
                                        bind<RTCViewModel>() with singleton { RTCViewModelImpl(rtcWebSocket) }
                                    }
                                }

                                startActivity(Intent(this, RTCActivity::class.java)
                                        .putExtra("roomName", roomName))
                            }
                            is SignalMessage.roomIsFull -> {
                                simpleAlert("room \"$roomName\" is full")
                                subscription?.unsubscribe()
                            }
                        }
                    }, {
                        simpleAlert("${it.message}")
                    }, { simpleAlert("connection closed") }
                    )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requestedPermissionResult(requestCode, permissions, grantResults)
    }
}
