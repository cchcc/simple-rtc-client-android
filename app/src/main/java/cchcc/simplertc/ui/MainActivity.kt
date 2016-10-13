package cchcc.simplertc.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import cchcc.simplertc.App
import cchcc.simplertc.G
import cchcc.simplertc.R
import cchcc.simplertc.ext.*
import cchcc.simplertc.inject.DaggerRTCWebSocketComponent
import cchcc.simplertc.inject.RTCWebSocketModule
import cchcc.simplertc.model.SignalMessage
import kotlinx.android.synthetic.main.act_main.*
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ws.WebSocket
import okhttp3.ws.WebSocketCall
import okhttp3.ws.WebSocketListener
import okio.Buffer
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException

class MainActivity : BaseActivity() {

    private var checkCameraPermission: Boolean = false
        get() = if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA)
                    , RC_PERMISSION_CAMERA)
            false
        }
        else
            true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

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

    private fun checkServerIsOn() {
        val webSocketCall = WebSocketCall.create(App.component.okHttpClient()
                , Request.Builder().url(G.SIGNAL_SERVER_ADDR).build())

        Observable.create<Boolean> { subscriber ->
            val serverStatusIs = { isOn: Boolean ->
                subscriber.onNext(isOn)
                subscriber.onCompleted()
            }

            webSocketCall.enqueue(object : WebSocketListener {
                override fun onOpen(webSocket: WebSocket?, response: Response?) {
                    webSocket?.close(1000, "")
                    serverStatusIs(true)
                }
                override fun onPong(payload: Buffer?) {}
                override fun onClose(code: Int, reason: String?) = serverStatusIs(false)
                override fun onFailure(e: IOException?, response: Response?) = serverStatusIs(false)
                override fun onMessage(message: ResponseBody?) {}
            })
        }.doOnSubscribe {
            AnimationUtils.loadAnimation(this, R.anim.clockwise_rotation).apply {
                repeatCount = Animation.INFINITE
                repeatMode = Animation.RESTART
            }.let { tv_server_status.startAnimation(it) }
        }.doOnUnsubscribe {
            tv_server_status.clearAnimation()
            webSocketCall.cancel()
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            if (it) {
                tv_server_status.text = "ON"
                tv_server_status.setTextColor(Color.GREEN)
            } else {
                tv_server_status.text = "OFF"
                tv_server_status.setTextColor(Color.RED)
            }
        }.addToComposite()
    }

    private fun connectToServerWithRoomName() {
        val roomName = et_room_name.text.toString()
        if (roomName.isBlank()) {
            et_room_name.nope()
            return
        }

        if (!checkCameraPermission)
            return

        preferences.save { it.putString(G.PFK_ROOM_NAME, roomName) }

        val rtcWebSocketComponent = DaggerRTCWebSocketComponent.builder()
                .appComponent(App.component)
                .rTCWebSocketModule(RTCWebSocketModule(G.SIGNAL_SERVER_ADDR, roomName))
                .build()

        var subscription: Subscription? = null
        subscription = rtcWebSocketComponent.rtcWebSocket().observable
                .doOnSubscribe { runOnUiThread { showLoading() } }
                .doOnError { hideLoading() }
                .doOnNext { hideLoading() }
                .doOnCompleted { hideLoading() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    when (it) {
                        is SignalMessage.roomCreated,
                        is SignalMessage.roomJoined -> startRTC@{
                            subscription?.unsubscribe()
                            RTCActivity.rtcComponents.put(roomName, rtcWebSocketComponent)
                            startActivity(Intent(this, RTCActivity::class.java)
                                    .putExtra("roomName", roomName))
                            finish()
                        }
                        is SignalMessage.roomIsFull -> simpleAlert("room \"$roomName\" is full")
                    }
                }
                , { simpleAlert("connection error : ${it.message}") }
                , { simpleAlert("connection closed") }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionAllGranted = grantResults.size == grantResults
                .filter { it == PackageManager.PERMISSION_GRANTED }.size
        when(requestCode) {
            RC_PERMISSION_CAMERA -> if (permissionAllGranted) {
                connectToServerWithRoomName()
                checkCameraPermission = true
            }
        }

    }

    companion object {
        val RC_PERMISSION_CAMERA = 1
    }
}
