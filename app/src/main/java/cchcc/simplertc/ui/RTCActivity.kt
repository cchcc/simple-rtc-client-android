package cchcc.simplertc.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import cchcc.simplertc.R
import cchcc.simplertc.ext.simpleAlert
import cchcc.simplertc.ext.toast
import cchcc.simplertc.inject.DaggerRTCViewModelComponent
import cchcc.simplertc.inject.PerRTCActivity
import cchcc.simplertc.inject.RTCViewModelModule
import cchcc.simplertc.inject.RTCWebSocketComponent
import cchcc.simplertc.model.ChatMessage
import cchcc.simplertc.viewmodel.RTCViewModel
import kotlinx.android.synthetic.main.act_rtc.*
import kotlinx.android.synthetic.main.li_chat_message.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerRTCActivity
class RTCActivity : BaseActivity() {
    @Inject lateinit var viewModel: RTCViewModel
    private val chatListAdapter: ChatListAdapter by lazy { ChatListAdapter() }
    private var passedTimeSubscript: Subscription? = null

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

        setContentView(R.layout.act_rtc)

        with(viewModel) {
            eventObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event@{ occurredViewModelEvent(it) }
                            , error@{ simpleAlert(it.toString()) }
                            , terminated@{ terminatedRTC() })
                    .addToComposite()
            onCreate(this@RTCActivity, glv_video)
        }

        tv_sending_message.setOnClickListener { clickedChatList() }
        bt_terminate.setOnClickListener { clickedTerminate() }
        with(rv_chat) {
            layoutManager = LinearLayoutManager(this@RTCActivity)
            adapter = chatListAdapter
        }
    }

    private fun occurredViewModelEvent(event: RTCViewModel.Event) {
        when(event) {
            is RTCViewModel.Event.Connected -> {
                tv_waiting.visibility = View.GONE
                chatListAdapter.addAndNotify(ChatMessage(Date(), "system", "Start"))

                passedTimeSubscript = Observable.interval(1, TimeUnit.SECONDS).startWith(0)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { tv_passed_time.text = "${it/60}:${it%60}" }
                    .apply { addToComposite() }
            }
            is RTCViewModel.Event.Chat -> chatListAdapter.addAndNotify(event.message)
        }
    }

    private fun terminatedRTC() {
        passedTimeSubscript?.unsubscribe()
        chatListAdapter.addAndNotify(ChatMessage(Date()
                , "system", getString(R.string.terminated)))
        toast(R.string.terminated)
    }

    private fun clickedTerminate() {
        viewModel.terminate()
        finish()
    }

    private fun clickedChatList() {
        val et_message = EditText(this)
        AlertDialog.Builder(this)
                .setTitle(R.string.sending_message)
                .setView(et_message)
                .setPositiveButton(R.string.send) { dlg, w ->
                    viewModel.sendChatMessage(et_message.text.toString())
                }.show()
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
        startActivity(Intent(this, MainActivity::class.java))
        super.onDestroy()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(R.string.are_you_sure_to_quit)
            .setPositiveButton(R.string.yes) { dlg, w -> super.onBackPressed() }
            .setNegativeButton(R.string.no) { dlg, w -> }
            .show()
    }

    inner class ChatListAdapter : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

        private val list = mutableListOf<ChatMessage>()
        private val chatDateFormat: SimpleDateFormat by lazy {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun addAndNotify(chatMessage: ChatMessage) {
            list.add(chatMessage)
            notifyDataSetChanged()
            rv_chat.scrollToPosition(list.size - 1)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder =
            ViewHolder(View.inflate(this@RTCActivity, R.layout.li_chat_message, parent))

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
            val chatMessage = list[position]
            tv_passed_time.text = chatDateFormat.format(chatMessage.dateTime)
            tv_sender.text = chatMessage.sender
            tv_message.text = chatMessage.message
        }

    }

    companion object {
        val rtcComponents: MutableMap<String/*room name*/, RTCWebSocketComponent> by lazy {
            mutableMapOf<String, RTCWebSocketComponent>()
        }
    }
}
