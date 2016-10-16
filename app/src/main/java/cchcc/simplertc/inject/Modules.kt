package cchcc.simplertc.inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.singleton
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object Modules {
    val okHttpClient = Kodein.Module {
        bind<OkHttpClient>() with singleton { OkHttpClient.Builder().readTimeout(0, TimeUnit.SECONDS).build() }
    }

    val jsonMapper = Kodein.Module {
        bind<ObjectMapper>() with singleton { jacksonObjectMapper() }
    }
}