package com.louaraya.macrame

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.room.Room
import com.louaraya.macrame.data.AppDatabase
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.SessionManager
import com.louaraya.macrame.data.remote.ApiProvider
import com.louaraya.macrame.data.remote.RemoteDataSources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Contenedor  */
class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: AppDatabase = Room
        .databaseBuilder(context.applicationContext, AppDatabase::class.java, "macrame.db")
        .fallbackToDestructiveMigration()
        .build()

    /** Cliente Retrofit único para microservicios propios y API externa. */
    val remote = RemoteDataSources(ApiProvider())

    val repository = MacrameRepository(database, remote)
    val session = SessionManager(context.applicationContext, repository, appScope)

    init {
        appScope.launch {
            repository.seedIfNeeded()
            // Si los microservicios están encendidos, se reciben los datos remotos al iniciar.
            repository.syncFromRemote()
        }
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no disponible: falta proveerlo en MainActivity")
}
