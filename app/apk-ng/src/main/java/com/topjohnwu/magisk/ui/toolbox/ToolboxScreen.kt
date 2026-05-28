package com.topjohnwu.magisk.ui.toolbox

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.arch.VMFactory
import com.topjohnwu.magisk.ui.navigation.Navigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen(navigator: Navigator) {
    val viewModel: ToolboxViewModel = viewModel(factory = VMFactory)
    val statusMessage by viewModel.statusMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
    }

    var showAppListDialog by remember { mutableStateOf(false) }
    var appListAction by remember { mutableStateOf("") } // "systemizer" or "backup"
    
    val userApps by viewModel.userApps.collectAsState()

    val downgradeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.downgradeApk(context, uri)
        }
    }

    val restoreModulesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.restoreModules(context, uri)
        }
    }

    if (showAppListDialog) {
        AlertDialog(
            onDismissRequest = { showAppListDialog = false },
            title = { Text(if (appListAction == "systemizer") "Select App to Systemize" else "Select App to Backup") },
            text = {
                if (userApps.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(userApps) { pkg ->
                            Text(
                                text = pkg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAppListDialog = false
                                        if (appListAction == "systemizer") {
                                            viewModel.systemizeApp(pkg)
                                        } else {
                                            viewModel.backupAppData(pkg)
                                        }
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppListDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Toolbox") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Power Tools", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
            
            ToolCard(
                title = "App Systemizer",
                description = "Convert user installed apps into system apps by mounting them via Magisk.",
                icon = Icons.Outlined.Transform,
                onClick = {
                    appListAction = "systemizer"
                    viewModel.loadUserApps()
                    showAppListDialog = true
                }
            )

            ToolCard(
                title = "Native App Downgrader",
                description = "Force install an older version of an app over a newer one without losing data.",
                icon = Icons.Outlined.KeyboardDoubleArrowDown,
                onClick = {
                    downgradeLauncher.launch("application/vnd.android.package-archive")
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Backup & Restore", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
            
            ToolCard(
                title = "Backup All Modules",
                description = "Package all active modules into a single tar.gz file in your Downloads folder.",
                icon = Icons.Outlined.Inventory2,
                onClick = { viewModel.backupModules() }
            )

            ToolCard(
                title = "Restore Modules",
                description = "Restore a previously backed up tar.gz module package.",
                icon = Icons.Outlined.Restore,
                onClick = { restoreModulesLauncher.launch("application/gzip") }
            )

            ToolCard(
                title = "App Data Backup",
                description = "Extract and compress the private /data/data/ directory of any installed app.",
                icon = Icons.Outlined.Save,
                onClick = {
                    appListAction = "backup"
                    viewModel.loadUserApps()
                    showAppListDialog = true
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ToolCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
