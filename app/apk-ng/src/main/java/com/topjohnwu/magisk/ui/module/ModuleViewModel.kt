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
