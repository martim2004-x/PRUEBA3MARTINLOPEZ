package com.louaraya.macrame.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/** API externa: Open-Meteo (sin API key). Se muestra el clima actual de Santiago en la app. */
interface WeatherApi {
    @GET("v1/forecast")
    suspend fun currentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code",
        @Query("timezone") timezone: String = "America/Santiago"
    ): WeatherResponse
}

data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather? = null,
    @SerializedName("current_units") val units: WeatherUnits? = null
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double? = null,
    @SerializedName("weather_code") val code: Int? = null,
    @SerializedName("time") val time: String? = null
)

data class WeatherUnits(
    @SerializedName("temperature_2m") val temperature: String? = null
)

/** Convierte el código WMO de Open-Meteo a texto y a un emoji para la interfaz. */
fun WeatherResponse.display(): String {
    val temp = current?.temperature
    if (temp == null) return "Clima no disponible"
    val (icon, text) = when (current?.code) {
        0 -> "☀️" to "Despejado"
        1, 2 -> "🌤️" to "Parcialmente nublado"
        3 -> "☁️" to "Nublado"
        45, 48 -> "🌫️" to "Niebla"
        in 51..67 -> "🌧️" to "Lluvia"
        in 71..77 -> "❄️" to "Nieve"
        in 80..82 -> "🌦️" to "Chubascos"
        in 95..99 -> "⛈️" to "Tormenta"
        else -> "🌡️" to "Clima actual"
    }
    return "$icon $text · ${temp.toInt()}°${units?.temperature ?: "C"}"
}
