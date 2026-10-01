package io.github.shashigm.videra

import android.app.Application
import io.github.shashigm.videra.di.VideraAppContainer

class VideraApplication : Application() {
    val container: VideraAppContainer by lazy {
        VideraAppContainer(this)
    }
}
