package com.topjohnwu.magisk.ui.module

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.topjohnwu.magisk.core.AppContext
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.ui.navigation.LocalNavigator
import com.topjohnwu.magisk.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleCreatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    var id by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("") }
    var versionCode by remember { mutableStateOf("") }
    var creator by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var script by remember { mutableStateOf("#!/system/bin/sh\n# Write your shell script here\n") }

    var showMenu by remember { mutableStateOf(false) }
    var createdZipUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            Toast.makeText(context, "Selected ${uris.size} files", Toast.LENGTH_SHORT).show()
        }
    }

    if (showMenu && createdZipUri != null) {
        ModalBottomSheet(onDismissRequest = { showMenu = false }) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text("Module Created!", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        showMenu = false
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, createdZipUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Module"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Share Module Zip")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        showMenu = false
                        navigator.push(Route.Flash(action = Const.Value.FLASH_ZIP, additionalData = createdZipUri.toString()))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Install Module")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Integrated Module Creator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val uri = withContext(Dispatchers.IO) {
                            try {
                                val moduleProp = "id=$id\nname=$title\nversion=$version\nversionCode=$versionCode\nauthor=$creator\ndescription=$description\n"
                                val zipFile = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "magisk_module_${id}.zip")
                                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                                    zos.putNextEntry(ZipEntry("module.prop"))
                                    zos.write(moduleProp.toByteArray())
                                    zos.closeEntry()
                                    
                                    zos.putNextEntry(ZipEntry("customize.sh"))
                                    zos.write(script.toByteArray())
                                    zos.closeEntry()
                                }
                                Uri.fromFile(zipFile)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (uri != null) {
                            createdZipUri = uri
                            showMenu = true
                        } else {
                            Toast.makeText(context, "Failed to build module", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Text("Build Module")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Module Properties",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("Module ID") },
                placeholder = { Text("e.g., my_awesome_module") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("Version") },
                    placeholder = { Text("1.0") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = versionCode,
                    onValueChange = { versionCode = it },
                    label = { Text("Version Code") },
                    placeholder = { Text("1") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = creator,
                onValueChange = { creator = it },
                label = { Text("Creator/Author") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Module Logic",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = script,
                onValueChange = { script = it },
                label = { Text("customize.sh (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Files into Module")
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}
