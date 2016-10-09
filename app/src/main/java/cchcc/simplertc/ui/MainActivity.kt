package cchcc.simplertc.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import cchcc.simplertc.App
import cchcc.simplertc.G
import cchcc.simplertc.R
import cchcc.simplertc.ext.*
import cchcc.simplertc.inject.DaggerRTCWebSocketComponent
import cchcc.simplertc.inject.RTCWebSocketModule
import cchcc.simplertc.model.SignalMessage
import kotlinx.android.synthetic.main.act_main.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private var subscriptionConnection: Subscription? = null

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
    }

    private fun connectToServerWithRoomName() {
        val roomName = et_room_name.text.toString()
        if (roomName.isBlank()) {
            et_room_name.nope()
            return
        }

        preferences.save { it.putString(G.PFK_ROOM_NAME, roomName) }

        val rtcWebSocketComponent = DaggerRTCWebSocketComponent.builder()
                .appComponent(App.component)
                .rTCWebSocketModule(RTCWebSocketModule(G.SIGNAL_SERVER_ADDR, roomName))
                .build()

        subscriptionConnection = rtcWebSocketComponent.rtcWebSocket().observable
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

    override fun onDestroy() {
        subscriptionConnection?.unsubscribe()
        subscriptionConnection = null
        super.onDestroy()
    }

}
