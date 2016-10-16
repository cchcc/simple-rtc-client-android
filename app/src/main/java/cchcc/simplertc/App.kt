package cchcc.simplertc

import android.app.Application
import cchcc.simplertc.inject.Modules
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.androidModule
import com.github.salomonbrys.kodein.lazy

class App : Application(), KodeinAware {
    override val kodein: Kodein by Kodein.lazy {
        import(androidModule)
        import(Modules.okHttpClient)
        import(Modules.jsonMapper)
    }

    override fun onCreate() {
        super.onCreate()
        App.kodein = kodein
    }

    companion object {
        lateinit var kodein: Kodein
    }
}