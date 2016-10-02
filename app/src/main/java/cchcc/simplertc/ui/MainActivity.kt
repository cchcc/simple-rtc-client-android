package cchcc.simplertc.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import cchcc.simplertc.G
import cchcc.simplertc.R
import cchcc.simplertc.ext.toast
import kotlinx.android.synthetic.main.act_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        tv_desc.text = "signal server : ${G.SIGNAL_SERVER_ADDR}"

        et_room_name.setOnEditorActionListener { textView, id, keyEvent ->
            if (id == EditorInfo.IME_ACTION_GO)
                connectToServerWithRoomName()
            true
        }

        bt_go.setOnClickListener { connectToServerWithRoomName() }
    }

    private fun connectToServerWithRoomName() {
        val roomName = et_room_name.text.toString()
        if (roomName.isBlank()) {
            toast("room name is empty")
            et_room_name.requestFocus()
            return
        }
    }
}
