package fi.lasicaine.nutrilicious.data.network

import fi.lasicaine.nutrilicious.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val API_KEY = BuildConfig.API_KEY
private const val BASE_URL = "https://api.nal.usda.gov/ndb/"

private val usdaClient by lazy { buildClient()}
val usdaApi: UsdaApi by lazy { usdaClient.create(UsdaApi::class.java)}

private fun buildClient(): Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(buildHttpClient())
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

private fun buildHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(loggingInerceptor())
    .addInterceptor(apiKeyInterceptor())
    .build()

private fun loggingInerceptor() = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}

private fun apiKeyInterceptor() = injectQueryParams("api_key" to API_KEY)

private fun injectQueryParams(vararg params: Pair<String, String>): Interceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    val urlWithParams = originalRequest.url().newBuilder()
        .apply { params.forEach { addQueryParameter(it.first, it.second) }}
        .build()
    val newRequest = originalRequest.newBuilder().url(urlWithParams).build()

    chain.proceed(newRequest)
}