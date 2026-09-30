package io.github.shashigm.videra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shashigm.videra.di.VideraAppContainer
import io.github.shashigm.videra.ui.navigation.MainScaffold
import io.github.shashigm.videra.ui.theme.VideraTheme

class MainActivity : ComponentActivity() {

    private val appContainer by lazy {
        VideraAppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val appTheme by appContainer.preferencesRepository
                .appTheme
                .collectAsStateWithLifecycle()

            VideraTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(
                        addonRepository = appContainer.addonRepository,
                        installAddonUseCase = appContainer.installAddonUseCase,
                        episodeProgressRepository = appContainer.episodeProgressRepository,
                        libraryRepository = appContainer.libraryRepository,
                        playerController = appContainer.playerController,
                        preferencesRepository = appContainer.preferencesRepository
                    )
                }
            }
        }
    }
}
