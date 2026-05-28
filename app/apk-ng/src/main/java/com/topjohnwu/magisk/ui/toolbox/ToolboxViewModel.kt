package com.topjohnwu.magisk.ui.toolbox

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.magisk.core.utils.RootUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToolboxViewModel : ViewModel() {

    private val _userApps = MutableStateFlow<List<String>>(emptyList())
    val userApps: StateFlow<List<String>> = _userApps

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    fun loadUserApps() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = Shell.cmd("pm list packages -3").exec()
                if (result.isSuccess) {
                    val apps = result.out.mapNotNull { it.removePrefix("package:") }
                    _userApps.value = apps.sorted()
                }
            }
        }
    }

    fun systemizeApp(packageName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _statusMessage.value = "Systemizing $packageName..."
                val pathResult = Shell.cmd("pm path $packageName").exec()
                if (!pathResult.isSuccess || pathResult.out.isEmpty()) {
                    _statusMessage.value = "Failed to find app path"
                    return@withContext
                }
                val apkPath = pathResult.out[0].removePrefix("package:")
                val moduleDir = "/data/adb/modules/systemizer_$packageName"
                
                Shell.cmd(
                    "mkdir -p $moduleDir/system/priv-app/$packageName",
                    "cp $apkPath $moduleDir/system/priv-app/$packageName/$packageName.apk",
                    "echo 'id=systemizer_$packageName\\nname=Systemizer: $packageName\\nversion=1.0\\nversionCode=1\\nauthor=Magisk Next\\ndescription=Systemized app overlay' > $moduleDir/module.prop",
                    "touch $moduleDir/update"
                ).exec()
                
                _statusMessage.value = "Success! Please reboot to apply."
            }
        }
    }

    fun downgradeApk(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _statusMessage.value = "Installing..."
                // Since resolving URI path for root shell is complex, we copy to cache first
                val cacheFile = java.io.File(context.cacheDir, "temp_install.apk")
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val result = Shell.cmd("pm install -r -d ${cacheFile.absolutePath}").exec()
                    if (result.isSuccess) {
                        _statusMessage.value = "Installation successful!"
                    } else {
                        _statusMessage.value = "Install failed: \n${result.err.joinToString("\\n")}"
                    }
                } catch (e: Exception) {
                    _statusMessage.value = "Error: ${e.message}"
                } finally {
                    cacheFile.delete()
                }
            }
        }
    }

    fun backupModules() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _statusMessage.value = "Backing up modules..."
                val backupPath = "/sdcard/Download/MagiskModulesBackup.tar.gz"
                val result = Shell.cmd("tar -czf $backupPath -C /data/adb modules").exec()
                if (result.isSuccess) {
                    _statusMessage.value = "Modules backed up to Downloads!"
                } else {
                    _statusMessage.value = "Backup failed."
                }
            }
        }
    }

    fun restoreModules(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _statusMessage.value = "Restoring modules..."
                val cacheFile = java.io.File(context.cacheDir, "temp_modules.tar.gz")
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val result = Shell.cmd(
                        "rm -rf /data/adb/modules/*",
                        "tar -xzf ${cacheFile.absolutePath} -C /data/adb"
                    ).exec()
                    if (result.isSuccess) {
                        _statusMessage.value = "Restore successful! Please reboot."
                    } else {
                        _statusMessage.value = "Restore failed."
                    }
                } catch (e: Exception) {
                    _statusMessage.value = "Error: ${e.message}"
                } finally {
                    cacheFile.delete()
                }
            }
        }
    }

    fun backupAppData(packageName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _statusMessage.value = "Backing up data for $packageName..."
                val backupPath = "/sdcard/Download/DataBackup_$packageName.tar.gz"
                val result = Shell.cmd("tar -czf $backupPath -C /data/data/$packageName .").exec()
                if (result.isSuccess) {
                    _statusMessage.value = "Data backed up to Downloads!"
                } else {
                    _statusMessage.value = "Backup failed."
                }
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
