package cchcc.simplertc.ui

import android.support.v7.app.AppCompatActivity
import rx.Subscription
import rx.subscriptions.CompositeSubscription

open class BaseActivity : AppCompatActivity() {
    val compositeSubscription = CompositeSubscription()

    fun Subscription.addToComposite() = compositeSubscription.add(this)

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        compositeSubscription.clear()
        super.onDestroy()
    }
}