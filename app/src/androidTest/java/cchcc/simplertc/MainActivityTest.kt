package cchcc.simplertc

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import cchcc.simplertc.ext.onNextAndCompleted
import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.model.SignalMessage
import cchcc.simplertc.ui.MainActivity
import cchcc.simplertc.viewmodel.MainViewModel
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.factory
import com.github.salomonbrys.kodein.singleton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import rx.lang.kotlin.observable

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule val activityRule = object : ActivityTestRule<MainActivity>(MainActivity::class.java) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()

            val appContext = InstrumentationRegistry.getTargetContext()

            val app = appContext.applicationContext as App
            app.kodein = Kodein {
                extend(app.kodein)

                bind<Kodein>(MainActivity::class, true) with singleton {
                    Kodein {
                        bind<MainViewModel>() with singleton {
                            object : MainViewModel {
                                override fun checkServerIsOn(): Observable<Boolean> = observable { it.onNextAndCompleted(true) }
                            }
                        }

                        bind<RTCWebSocket>() with factory { s: String ->
                            object : RTCWebSocket {
                                override val isConnected: Boolean
                                    get() = throw UnsupportedOperationException()
                                override val messageObservable: Observable<SignalMessage>
                                    get() = throw UnsupportedOperationException()

                                override fun send(message: SignalMessage) {
                                }

                                override fun close() {
                                }

                            }
                        }
                    }
                }


            }
        }
    }


    @Test
    fun serverIsOn_must_be_on() {
        onView(withId(R.id.tv_server_status))
                .check(ViewAssertions.matches(withText("ON")))
    }

}
