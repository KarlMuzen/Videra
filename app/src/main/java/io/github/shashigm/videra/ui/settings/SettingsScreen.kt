package io.github.shashigm.videra.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.shashigm.videra.data.preferences.AppTheme

@Composable
fun SettingsScreen(
    appTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    onManageAddons: () -> Unit,
    lastCrashLog: String?,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                val themes = listOf(
                    AppTheme.VELVET_RED to "Velvet Red",
                    AppTheme.OLED_BLACK to "OLED Black"
                )

                themes.forEachIndexed { index, (value, label) ->
                    SegmentedButton(
                        selected = appTheme == value,
                        onClick = { onThemeChanged(value) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = themes.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(label)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Diagnostics & Logs",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (lastCrashLog.isNullOrBlank()) {
                        Text(
                            text = "No crashes recorded",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = lastCrashLog
                                .lineSequence()
                                .take(4)
                                .joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = onClearLogs,
                        enabled = !lastCrashLog.isNullOrBlank()
                    ) {
                        Text("Clear Logs")
                    }
                }
            }

            Button(
                onClick = onManageAddons,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage add-ons")
            }
        }
    }
}
