package com.example.life4pollinators

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.osmdroid.config.Configuration

/**
 * Application class dell'app, inizializza Koin per la dependency injection.
 */
class Life4PollinatorsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configura OSMDroid PRIMA di Koin
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = cacheDir
            osmdroidTileCache = getExternalFilesDir(null)
        }

        // Avvia Koin con il modulo delle dipendenze dell'app
        startKoin {
            androidLogger()
            androidContext(this@Life4PollinatorsApplication)
            modules(appModule)
        }
    }
}