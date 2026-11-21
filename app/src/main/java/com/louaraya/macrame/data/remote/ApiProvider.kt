package com.louaraya.macrame.data.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Construye los clientes Retrofit. Se puede sustituir en pruebas. */
class ApiProvider(
    catalogUrl: String = ApiConfig.CATALOG_URL,
    requestUrl: String = ApiConfig.REQUEST_URL,
    weatherUrl: String = ApiConfig.WEATHER_URL,
    private val httpClient: OkHttpClient = defaultClient()
) {
    private val gson = GsonBuilder().setLenient().create()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val catalogApi: CatalogApi = retrofit(catalogUrl).create(CatalogApi::class.java)
    val requestApi: RequestApi = retrofit(requestUrl).create(RequestApi::class.java)
    val weatherApi: WeatherApi = retrofit(weatherUrl).create(WeatherApi::class.java)

    companion object {
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
    }
}
