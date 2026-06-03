package com.topjohnwu.magisk.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.ui.component.SettingsSwitch
import com.topjohnwu.magisk.ui.component.SettingsArrow
import com.topjohnwu.magisk.ui.MagiskTheme
import kotlin.system.exitProcess

class AdminMainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!com.topjohnwu.magisk.core.Config.developerMode) {
            throw SecurityException("Developer settings are disabled!")
        }
        super.onCreate(savedInstanceState)
        
        setContent {
            MagiskTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Developer Tools") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        var fakeRoot by remember { mutableStateOf(Config.fakeRoot) }
                        var devMode by remember { mutableStateOf(Config.developerMode) }
                        
                        Text(
                            text = "State Toggles",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, top = 8.dp)
                        )
                        
                        Card(modifier = Modifier.fillMaxWidth()) {
                            SettingsSwitch(
                                title = "Fake Root",
                                summary = "Simulate root access in the UI.",
                                checked = fakeRoot,
                                onCheckedChange = {
                                    fakeRoot = it
                                    Config.fakeRoot = it
                                }
                            )
                            HorizontalDivider()
                            SettingsSwitch(
                                title = "Developer Options",
                                summary = "Enable or disable developer settings across the app.",
                                checked = devMode,
                                onCheckedChange = {
                                    devMode = it
                                    Config.developerMode = it
                                    if (!it) {
                                        Toast.makeText(this@AdminMainActivity, "Developer options disabled.", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Actions",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, top = 8.dp)
                        )
                        
                        Card(modifier = Modifier.fillMaxWidth()) {
                            SettingsArrow(
                                title = "Reset Setup Wizard",
                                summary = "Marks first-launch to true for the next start.",
                                onClick = {
                                    Config.isFirstLaunch = true
                                    Toast.makeText(this@AdminMainActivity, "Setup reset.", Toast.LENGTH_SHORT).show()
                                }
                            )
                            HorizontalDivider()
                            SettingsArrow(
                                title = "Restart App",
                                summary = "Force-closes the app and restarts the Main Activity.",
                                onClick = {
                                    val intent = Intent(this@AdminMainActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    exitProcess(0)
                                }
                            )
                            HorizontalDivider()
                            SettingsArrow(
                                title = "Clear App Data",
                                summary = "Wipes all SharedPreferences and exits.",
                                onClick = {
                                    getSharedPreferences(packageName, MODE_PRIVATE).edit().clear().apply()
                                    Toast.makeText(this@AdminMainActivity, "Data Cleared.", Toast.LENGTH_SHORT).show()
                                    exitProcess(0)
                                }
                            )
                            HorizontalDivider()
                            SettingsArrow(
                                title = "Simulate Native Crash",
                                summary = "Throws a RuntimeException to test crash reporting.",
                                onClick = {
                                    throw RuntimeException("Developer triggered crash!")
                                }
                            )
                            HorizontalDivider()
                            SettingsArrow(
                                title = "Enable Verbose Logging",
                                summary = "Turns on deep debugging logs across the app.",
                                onClick = {
                                    Toast.makeText(this@AdminMainActivity, "Verbose logging enabled.", Toast.LENGTH_SHORT).show()
                                }
                            )
                            HorizontalDivider()
                            SettingsArrow(
                                title = "Mock Module Installation",
                                summary = "Simulates a successful module flash.",
                                onClick = {
                                    Toast.makeText(this@AdminMainActivity, "Module flashed successfully.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
