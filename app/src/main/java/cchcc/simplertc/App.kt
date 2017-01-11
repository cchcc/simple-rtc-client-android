package cchcc.simplertc

import android.app.Application
import cchcc.simplertc.inject.objectMapperModule
import cchcc.simplertc.inject.okHttpClientModule
import cchcc.simplertc.model.RTCWebSocket
import cchcc.simplertc.model.RTCWebSocketImpl
import cchcc.simplertc.ui.MainActivity
import cchcc.simplertc.ui.RTCActivity
import cchcc.simplertc.viewmodel.MainViewModel
import cchcc.simplertc.viewmodel.MainViewModelImpl
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.conf.ConfigurableKodein

class App : Application(), KodeinAware {
    override var kodein = Kodein {
        import(androidModule)
        import(okHttpClientModule)
        import(objectMapperModule)
        constant("serverAddress") with G.SIGNAL_SERVER_ADDR


        bind<Kodein>(MainActivity::class) with singleton {
            Kodein {
                extend(appKodein())
                bind<MainViewModel>() with singleton { MainViewModelImpl() }
                bind<RTCWebSocket>() with factory {
                    roomName: String -> RTCWebSocketImpl(instance(), instance(), instance("serverAddress"), roomName)
                }
            }
        }

        bind<Kodein>(RTCActivity::class) with singleton { ConfigurableKodein(true) }
    }
}