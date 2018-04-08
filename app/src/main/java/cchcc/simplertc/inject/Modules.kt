package cchcc.simplertc.inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


val okHttpClientModule = Kodein.Module {
    bind<OkHttpClient>() with singleton { OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(0, TimeUnit.SECONDS).build() }
}

val objectMapperModule = Kodein.Module {
    bind<ObjectMapper>() with singleton { jacksonObjectMapper() }
}
