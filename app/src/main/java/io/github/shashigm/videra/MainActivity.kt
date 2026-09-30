package io.github.shashigm.videra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.shashigm.videra.di.VideraAppContainer
import io.github.shashigm.videra.ui.navigation.MainScaffold

class MainActivity : ComponentActivity() {

    private val appContainer by lazy {
        VideraAppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(
                        addonRepository = appContainer.addonRepository,
                        libraryRepository = appContainer.libraryRepository,
                        playerController = appContainer.playerController
                    )
                }
            }
        }
    }
}
