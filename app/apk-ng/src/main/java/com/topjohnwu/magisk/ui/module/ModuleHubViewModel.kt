package com.topjohnwu.magisk.ui.module

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.magisk.core.di.ServiceLocator
import com.topjohnwu.magisk.core.model.module.OnlineModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ModuleHubViewModel : ViewModel() {

    var modules by mutableStateOf<List<OnlineModule>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isError by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    fun refresh() {
        if (isLoading) return
        isLoading = true
        isError = false
        viewModelScope.launch {
            try {
                val index = withContext(Dispatchers.IO) {
                    ServiceLocator.networkService.fetchModuleHubIndex()
                }
                modules = index.modules
            } catch (e: Exception) {
                Timber.e(e)
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
}
