package com.topjohnwu.magisk.ui.module

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.topjohnwu.magisk.arch.AsyncLoadViewModel
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.R as CoreR
import com.topjohnwu.magisk.core.download.Subject
import com.topjohnwu.magisk.core.model.module.LocalModule
import com.topjohnwu.magisk.core.model.module.OnlineModule
import com.topjohnwu.magisk.core.utils.TextHolder
import com.topjohnwu.magisk.core.utils.asText
import com.topjohnwu.magisk.ui.flash.FlashUtils
import com.topjohnwu.magisk.ui.navigation.Route
import com.topjohnwu.magisk.view.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

import com.topjohnwu.superuser.Shell

class ModuleItem(val module: LocalModule, val hasWebUI: Boolean = false) {
    val showNotice: Boolean
    val showAction: Boolean
    val noticeText: TextHolder

    init {
        val isZygisk = module.isZygisk
        val isRiru = module.isRiru
        val zygiskUnloaded = isZygisk && module.zygiskUnloaded

        showNotice = zygiskUnloaded ||
            (Info.isZygiskEnabled && isRiru) ||
            (!Info.isZygiskEnabled && isZygisk)
        showAction = module.hasAction && !showNotice
        noticeText =
            when {
                zygiskUnloaded -> CoreR.string.zygisk_module_unloaded.asText()
                isRiru -> CoreR.string.suspend_text_riru.asText(CoreR.string.zygisk.asText())
                else -> CoreR.string.suspend_text_zygisk.asText(CoreR.string.zygisk.asText())
            }
    }

    var isEnabled by mutableStateOf(module.enable)
    var isRemoved by mutableStateOf(module.remove)
    var showUpdate by mutableStateOf(module.updateInfo != null)
    val isUpdated = module.updated
    val updateReady get() = module.outdated && !isRemoved && isEnabled
}

@Parcelize
class OnlineModuleSubject(
    override val module: OnlineModule,
    override val autoLaunch: Boolean,
    override val notifyId: Int = Notifications.nextId()
) : Subject.Module() {
    override fun pendingIntent(context: Context) = FlashUtils.installIntent(context, file)
}

class ModuleViewModel : AsyncLoadViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val modules: List<ModuleItem> = emptyList(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    override suspend fun doLoadWork() {
        _uiState.update { it.copy(loading = true) }
        
        if (com.topjohnwu.magisk.core.Config.fakeRoot) {
            val fakeModuleDir = com.topjohnwu.magisk.core.utils.RootUtils.fs.getFile(com.topjohnwu.magisk.core.AppContext.cacheDir.absolutePath, "fake_module")
            fakeModuleDir.mkdirs()
            
            val fakeLocalModule = LocalModule(fakeModuleDir).apply {
                author = "Magisk Next"
                description = "This is a mock module to demonstrate the UI when Fake Root is enabled."
            }
            try {
                fakeLocalModule.javaClass.superclass?.getDeclaredField("id")?.apply { isAccessible = true }?.set(fakeLocalModule, "fake_module")
                fakeLocalModule.javaClass.superclass?.getDeclaredField("name")?.apply { isAccessible = true }?.set(fakeLocalModule, "Fake Module")
                fakeLocalModule.javaClass.superclass?.getDeclaredField("version")?.apply { isAccessible = true }?.set(fakeLocalModule, "1.0.0")
                fakeLocalModule.javaClass.superclass?.getDeclaredField("versionCode")?.apply { isAccessible = true }?.set(fakeLocalModule, 1)

                // Inject a fake webroot for the mock UI
                val webroot = com.topjohnwu.magisk.core.utils.RootUtils.fs.getFile(fakeModuleDir.absolutePath, "webroot")
                webroot.mkdirs()
                val indexHtml = com.topjohnwu.magisk.core.utils.RootUtils.fs.getFile(webroot.absolutePath, "index.html")
                indexHtml.writeText(
                    """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Fake Module Dashboard</title>
                        <style>
                            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: #121212; color: #ffffff; padding: 20px; }
                            h1 { font-weight: 300; }
                            .card { background: #1e1e1e; border-radius: 12px; padding: 20px; margin-top: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.3); }
                            button { background: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 6px; font-size: 16px; margin-top: 10px; cursor: pointer; }
                        </style>
                    </head>
                    <body>
                        <h1>Mock Dashboard</h1>
                        <p>This is a functional test of the MagiskNext WebUI Bridge.</p>
                        <div class="card">
                            <h3>Test Root Access</h3>
                            <button onclick="testRoot()">Run 'id' Command</button>
                            <pre id="output" style="margin-top: 15px; background: #000; padding: 10px; border-radius: 6px;"></pre>
                        </div>
                        <script>
                            function testRoot() {
                                try {
                                    document.getElementById('output').innerText = MagiskJS.exec('id');
                                } catch(e) {
                                    document.getElementById('output').innerText = 'Error: ' + e.message;
                                }
                            }
                        </script>
                    </body>
                    </html>
                    """.trimIndent()
                )
            } catch (e: Exception) {
                // Ignore
            }
            
            val fakeModules = listOf(ModuleItem(fakeLocalModule, hasWebUI = true))
            _uiState.update { it.copy(loading = false, modules = fakeModules) }
            return
        }

        val moduleLoaded = Info.env.isActive &&
            withContext(Dispatchers.IO) { LocalModule.loaded() }
        if (moduleLoaded) {
            val webUIs = withContext(Dispatchers.IO) {
                Shell.cmd("ls -d /data/adb/modules/*/webroot").exec().out
                    .mapNotNull { it.substringAfter("/modules/").substringBefore("/webroot") }
                    .toSet()
            }
            val modules = withContext(Dispatchers.Default) {
                LocalModule.installed().map { ModuleItem(it, webUIs.contains(it.id)) }
            }
            _uiState.update { it.copy(loading = false, modules = modules) }
            loadUpdateInfo()
        } else {
            _uiState.update { it.copy(loading = false) }
        }
    }

    private val networkObserver: (Boolean) -> Unit = { startLoading() }

    init {
        Info.isConnected.observeForever(networkObserver)
    }

    override fun onCleared() {
        super.onCleared()
        Info.isConnected.removeObserver(networkObserver)
    }

    private suspend fun loadUpdateInfo() {
        withContext(Dispatchers.IO) {
            _uiState.value.modules.forEach { item ->
                if (item.module.fetch()) {
                    item.showUpdate = item.module.updateInfo != null
                }
            }
        }
    }

    fun confirmLocalInstall(uri: Uri) {
        navigateTo(Route.Flash(Const.Value.FLASH_ZIP, uri.toString()))
    }

    fun runAction(id: String, name: String) {
        navigateTo(Route.Action(id, name))
    }

    fun openWebUI(context: Context, id: String, name: String) {
        context.startActivity(WebUIActivity.intent(context, id, name))
    }

    fun toggleEnabled(item: ModuleItem) {
        item.isEnabled = !item.isEnabled
        item.module.enable = item.isEnabled
    }

    fun toggleRemove(item: ModuleItem) {
        item.isRemoved = !item.isRemoved
        item.module.remove = item.isRemoved
    }
}
