package cchcc.simplertc

import android.opengl.GLSurfaceView
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import cchcc.simplertc.model.ChatMessage
import cchcc.simplertc.ui.RTCActivity
import cchcc.simplertc.viewmodel.RTCViewModel
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@RunWith(AndroidJUnit4::class)
class RTCActivityTest {
    @get:Rule val activityRule = object : ActivityTestRule<RTCActivity>(RTCActivity::class.java) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()
            val app = InstrumentationRegistry.getTargetContext().applicationContext as App
            app.kodein = Kodein {
                extend(app.kodein)

                bind<Kodein>(RTCActivity::class, true) with singleton {
                    Kodein { bind<RTCViewModel>() with singleton { rtcViewModel } }
                }
            }
        }

        override fun afterActivityLaunched() {
            super.afterActivityLaunched()
            eventSubject.onNext(RTCViewModel.Event.Connected())
        }
    }

    private val eventSubject by lazy { PublishSubject.create<RTCViewModel.Event>() }

    private val rtcViewModel = object : RTCViewModel {
        override val eventObservable: Observable<RTCViewModel.Event> by lazy { eventSubject.asObservable() }

        override fun onCreate(kodein: Kodein) {
            val glView = kodein.instance<GLSurfaceView>()
            glView.setRenderer(object : GLSurfaceView.Renderer{
                override fun onDrawFrame(gl: GL10?) {}

                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                    gl?.glViewport(0, 0, width, height)
                }

                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                    gl?.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
                }

            })
        }

        override fun onDestroy() {}

        override fun sendChatMessage(msg: String) {
            eventSubject.onNext(RTCViewModel.Event.Chat(ChatMessage(Date(), "me", msg)))
        }

        override fun terminate() {}
    }


    @Test
    fun receivedChat__display_well() {
        // given
        val message = ChatMessage(Date(), "test sender", "test hi")

        // when
        eventSubject.onNext(RTCViewModel.Event.Chat(message))

//        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // then
        onView(RecyclerViewMatcher(R.id.rv_chat).atPositionOnView(1, R.id.tv_sender))
                .check(ViewAssertions.matches(withText(message.sender)))

        onView(RecyclerViewMatcher(R.id.rv_chat).atPositionOnView(1, R.id.tv_message))
                .check(ViewAssertions.matches(withText(message.message)))

    }


    @Test
    fun sendChatMessage__display_well() {
        // given
        val message = "test hi"

        // when
        rtcViewModel.sendChatMessage(message)

        // then
        onView(RecyclerViewMatcher(R.id.rv_chat).atPositionOnView(1, R.id.tv_message))
                .check(ViewAssertions.matches(withText(message)))
    }
}