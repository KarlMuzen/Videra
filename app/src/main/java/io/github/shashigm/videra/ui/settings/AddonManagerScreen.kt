package io.github.shashigm.videra.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shashigm.videra.domain.model.InstalledAddon

@Composable
fun AddonManagerScreen(
    viewModel: AddonManagerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val installedAddons by viewModel.installedAddons.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var renameAddon by remember { mutableStateOf<InstalledAddon?>(null) }
    var deleteAddon by remember { mutableStateOf<InstalledAddon?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back to settings"
                )
            }

            Text(
                text = "Add-on Manager",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    viewModel.clearMessage()
                    showAddDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add add-on"
                )
            }
        }

        if (uiState.errorMessage != null) {
            ManagerMessageCard(
                message = uiState.errorMessage.orEmpty(),
                isError = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        } else if (uiState.successMessage != null) {
            ManagerMessageCard(
                message = uiState.successMessage.orEmpty(),
                isError = false,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (installedAddons.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "No add-ons installed",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Add a remote Videra manifest URL to provide content to Discover, Search, and Feed.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add add-on")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = installedAddons,
                    key = { addon -> addon.id }
                ) { addon ->
                    AddonCard(
                        addon = addon,
                        working = uiState.workingAddonId == addon.id,
                        onToggle = { viewModel.setEnabled(addon.id, !addon.enabled) },
                        onRename = { renameAddon = addon },
                        onDelete = { deleteAddon = addon }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAddonDialog(
            uiState = uiState,
            onDismiss = {
                if (!uiState.isInstalling) {
                    showAddDialog = false
                    viewModel.clearMessage()
                }
            },
            onInstall = viewModel::installAddon
        )
    }

    renameAddon?.let { addon ->
        RenameAddonDialog(
            addon = addon,
            working = uiState.workingAddonId == addon.id,
            onDismiss = {
                renameAddon = null
                viewModel.clearMessage()
            },
            onRename = { customName ->
                viewModel.renameAddon(addon.id, customName)
            }
        )
    }

    deleteAddon?.let { addon ->
        AlertDialog(
            onDismissRequest = { deleteAddon = null },
            title = { Text("Delete add-on?") },
            text = {
                Text(
                    "Remove " + addon.name + " from this device? Library snapshots are kept."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteAddon = null
                        viewModel.removeAddon(addon.id)
                    },
                    enabled = uiState.workingAddonId == null
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteAddon = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AddonCard(
    addon: InstalledAddon,
    working: Boolean,
    onToggle: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = addon.name,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = if (addon.enabled) "Active" else "Inactive",
                style = MaterialTheme.typography.bodyMedium,
                color = if (addon.enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (addon.customName != null && addon.customName != addon.manifestName) {
                Text(
                    text = "Manifest name: " + addon.manifestName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Version " + addon.version,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = addon.manifestUrl,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (addon.cachedMetadataJson != null) {
                    "Manifest metadata cached locally"
                } else {
                    "Manifest metadata not cached"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggle,
                    enabled = !working,
                    modifier = Modifier.weight(1f)
                ) {
                    if (working) {
                        CircularProgressIndicator()
                    } else {
                        Text(if (addon.enabled) "Disable" else "Enable")
                    }
                }

                OutlinedButton(
                    onClick = onRename,
                    enabled = !working
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null
                    )
                    Text("Rename")
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !working
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete " + addon.name
                    )
                }
            }
        }
    }
}

@Composable
private fun AddAddonDialog(
    uiState: AddonManagerUiState,
    onDismiss: () -> Unit,
    onInstall: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    val completed = uiState.successMessage != null && !uiState.isInstalling

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add remote add-on") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (completed) {
                    Text(uiState.successMessage.orEmpty())
                } else {
                    Text(
                        text = "Paste the HTTPS or HTTP URL of a Videra JSON manifest.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextField(
                        value = url,
                        onValueChange = { url = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Manifest URL") },
                        singleLine = true,
                        enabled = !uiState.isInstalling
                    )
                }
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (uiState.isInstalling) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Fetching and validating manifest…")
                    }
                }
            }
        },
        confirmButton = {
            if (completed) {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            } else {
                Button(
                    onClick = { onInstall(url) },
                    enabled = url.isNotBlank() && !uiState.isInstalling
                ) {
                    Text("Install")
                }
            }
        },
        dismissButton = {
            if (!completed) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !uiState.isInstalling
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun RenameAddonDialog(
    addon: InstalledAddon,
    working: Boolean,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember(addon.id) {
        mutableStateOf(addon.customName.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename add-on") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Custom name") },
                placeholder = { Text(addon.manifestName) },
                singleLine = true,
                enabled = !working
            )
        },
        confirmButton = {
            Button(
                onClick = { onRename(name) },
                enabled = !working
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ManagerMessageCard(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}