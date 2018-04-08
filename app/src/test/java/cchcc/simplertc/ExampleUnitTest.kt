package cchcc.simplertc

import cchcc.simplertc.ext.onNextAndCompleted
import cchcc.simplertc.viewmodel.MainViewModelImpl
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import rx.lang.kotlin.observable

/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        val viewModel = mock<MainViewModelImpl> {
            on { checkServerIsOn() }.thenReturn(observable { it.onNextAndCompleted(true) })
        }


        viewModel.checkServerIsOn()

    }
}