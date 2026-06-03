package com.topjohnwu.magisk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.VMFactory
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.model.module.LocalModule
import com.topjohnwu.magisk.ui.home.HomeScreen
import com.topjohnwu.magisk.ui.home.HomeViewModel
import com.topjohnwu.magisk.ui.install.InstallViewModel
import com.topjohnwu.magisk.ui.log.LogScreen
import com.topjohnwu.magisk.ui.log.LogViewModel
import com.topjohnwu.magisk.ui.module.ModuleScreen
import com.topjohnwu.magisk.ui.module.ModuleViewModel
import com.topjohnwu.magisk.ui.navigation.CollectNavEvents
import com.topjohnwu.magisk.ui.navigation.LocalNavigator
import com.topjohnwu.magisk.ui.settings.SettingsScreen
import com.topjohnwu.magisk.ui.settings.SettingsViewModel
import com.topjohnwu.magisk.ui.superuser.SuperuserScreen
import com.topjohnwu.magisk.ui.superuser.SuperuserViewModel
import kotlinx.coroutines.launch
import com.topjohnwu.magisk.core.R as CoreR

enum class Tab(val titleRes: Int, val iconRes: Int) {
    HOME(CoreR.string.section_home, R.drawable.ic_home),
    SU_DASHBOARD(CoreR.string.superuser, CoreR.drawable.ic_superuser),
    TOOLBOX(CoreR.string.toolbox, R.drawable.ic_toolbox),
    SETTINGS(CoreR.string.settings, R.drawable.ic_settings);
}

@Composable
fun MainScreen(initialTab: Int = Tab.HOME.ordinal) {
    val navigator = LocalNavigator.current
    val visibleTabs = remember {
        Tab.entries.filter { tab ->
            when (tab) {
                Tab.SU_DASHBOARD -> com.topjohnwu.magisk.core.Config.fakeRoot || Info.showSuperUser || (Info.env.isActive && LocalModule.loaded())
                Tab.TOOLBOX -> com.topjohnwu.magisk.core.Config.enableToolbox && (com.topjohnwu.magisk.core.Config.fakeRoot || Info.env.isActive)
                else -> true
            }
        }
    }
    val initialPage = visibleTabs.indexOf(Tab.entries.getOrNull(initialTab) ?: Tab.HOME).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { visibleTabs.size })

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = visibleTabs.size - 1,
            userScrollEnabled = true,
        ) { page ->
            when (visibleTabs[page]) {
                Tab.HOME -> {
                    val vm: HomeViewModel = viewModel(factory = VMFactory)
                    val installVm: InstallViewModel = viewModel(factory = VMFactory)
                    LaunchedEffect(Unit) { vm.startLoading() }
                    CollectNavEvents(vm, navigator)
                    CollectNavEvents(installVm, navigator)
                    HomeScreen(vm, installVm)
                }
                Tab.SU_DASHBOARD -> {
                    val activity = LocalActivity.current as MainActivity
                    val suVm: SuperuserViewModel = viewModel(viewModelStoreOwner = activity, factory = VMFactory)
                    val modVm: ModuleViewModel = viewModel(factory = VMFactory)
                    LaunchedEffect(Unit) {
                        suVm.authenticate = { onSuccess ->
                            activity.extension.withAuthentication { if (it) onSuccess() }
                        }
                        suVm.startLoading()
                        modVm.startLoading()
                    }
                    CollectNavEvents(modVm, navigator)
                    com.topjohnwu.magisk.ui.superuser.SuDashboardScreen(suVm, modVm)
                }
                Tab.TOOLBOX -> {
                    com.topjohnwu.magisk.ui.toolbox.ToolboxScreen(navigator = navigator)
                }
                Tab.SETTINGS -> {
                    val activity = LocalActivity.current as MainActivity
                    val vm: SettingsViewModel = viewModel(factory = VMFactory)
                    LaunchedEffect(Unit) {
                        vm.authenticate = { onSuccess ->
                            activity.extension.withAuthentication { if (it) onSuccess() }
                        }
                    }
                    CollectNavEvents(vm, navigator)
                    SettingsScreen(vm)
                }
            }
        }

        FloatingNavigationBar(
            pagerState = pagerState,
            visibleTabs = visibleTabs,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FloatingNavigationBar(
    pagerState: PagerState,
    visibleTabs: List<Tab>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(28.dp)
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    NavigationBar(
        modifier = modifier
            .padding(bottom = navBarInset + 12.dp, start = 24.dp, end = 24.dp)
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .fillMaxWidth()
            .height(64.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        visibleTabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(tab.iconRes),
                        contentDescription = stringResource(tab.titleRes),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.titleRes),
                        fontSize = 11.sp
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                alwaysShowLabel = true
            )
        }
    }
}

