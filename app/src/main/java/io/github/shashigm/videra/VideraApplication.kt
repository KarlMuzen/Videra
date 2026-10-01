package io.github.shashigm.videra

import android.app.Application
import io.github.shashigm.videra.di.VideraAppContainer
import io.github.shashigm.videra.diagnostics.CrashHandler

class VideraApplication : Application() {
    override fun onCreate() {
        CrashHandler.install(this)
        super.onCreate()
    }

    val container: VideraAppContainer by lazy {
        VideraAppContainer(this)
    }
}
