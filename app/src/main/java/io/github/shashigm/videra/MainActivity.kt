package io.github.shashigm.videra

import io.github.shashigm.videra.di.VideraAppContainer
import io.github.shashigm.videra.diagnostics.CrashHandler

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import io.github.shashigm.videra.ui.navigation.MainScaffold
import io.github.shashigm.videra.ui.theme.VideraTheme

class MainActivity : ComponentActivity() {

    private val appContainer: VideraAppContainer
        get() = (application as VideraApplication).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val previousCrashLog = CrashHandler.getLastCrashLog(applicationContext)

        setContent {
            var showCrashDialog by remember {
                mutableStateOf(!previousCrashLog.isNullOrBlank())
            }

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

            if (showCrashDialog && !previousCrashLog.isNullOrBlank()) {
                CrashDiagnosticDialog(
                    crashLog = previousCrashLog,
                    onCopyTrace = {
                        getSystemService(
                            ClipboardManager::class.java
                        )?.setPrimaryClip(
                            ClipData.newPlainText(
                                "Videra crash trace",
                                previousCrashLog
                            )
                        )
                    },
                    onDismissAndClear = {
                        appContainer.preferencesRepository.clearLastCrashLog()
                        showCrashDialog = false
                    }
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CrashDiagnosticDialog(
        crashLog: String,
        onCopyTrace: () -> Unit,
        onDismissAndClear: () -> Unit
    ) {
        val preview = crashLog
            .lineSequence()
            .take(15)
            .joinToString("\n")

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Crash Detected on Previous Launch")
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = preview,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = onDismissAndClear) {
                    Text("Dismiss / Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = onCopyTrace) {
                    Text("Copy Trace")
                }
            }
        )
    }
}
