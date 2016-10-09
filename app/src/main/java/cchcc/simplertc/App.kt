package cchcc.simplertc

import android.app.Application
import cchcc.simplertc.inject.AppComponent
import cchcc.simplertc.inject.AppModule
import cchcc.simplertc.inject.DaggerAppComponent

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    companion object {
        lateinit var component: AppComponent
    }
}