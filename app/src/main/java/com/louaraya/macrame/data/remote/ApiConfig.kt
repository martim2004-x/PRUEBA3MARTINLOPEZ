package com.louaraya.macrame.data.remote

/**
 * URLs base de los servicios.
 * 10.0.2.2 es el host visto desde el emulador de Android.
 * Para un dispositivo físico, reemplaza 10.0.2.2 por la IP de tu computador.
 */
object ApiConfig {
    const val CATALOG_URL = "http://10.0.2.2:8081/"
    const val REQUEST_URL = "http://10.0.2.2:8082/"
    const val WEATHER_URL = "https://api.open-meteo.com/"

    /** Latitud/longitud de Santiago, usada para mostrar el clima de las ferias. */
    const val SANTIAGO_LAT = -33.4489
    const val SANTIAGO_LON = -70.6693
}
