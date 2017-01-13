package cchcc.simplertc

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import cchcc.simplertc.ext.onNextAndCompleted
import cchcc.simplertc.ui.MainActivity
import cchcc.simplertc.viewmodel.MainViewModel
import cchcc.simplertc.viewmodel.MainViewModelImpl
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.singleton
import com.nhaarman.mockito_kotlin.mock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.lang.kotlin.observable


@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    init {
        System.setProperty("MOCK_MAKER", "mock-maker-inline")
    }

    @get:Rule val rule = object : ActivityTestRule<MainActivity>(MainActivity::class.java) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()

            val appContext = InstrumentationRegistry.getTargetContext()
            val app = appContext.applicationContext as App
            app.kodein = Kodein {
                extend(app.kodein)

                bind<Kodein>(MainActivity::class, true) with singleton {
                    Kodein {
                        bind<MainViewModel>() with singleton {
                            mock<MainViewModelImpl> {
                                on { checkServerIsOn() }.thenReturn(observable {
                                    it.onNextAndCompleted(true)
                                }

                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun serverIsOn_should_on() {
        onView(withId(R.id.tv_server_status))
                .check(ViewAssertions.matches(isDisplayed()))
    }
}