package com.topjohnwu.magisk.ui.superuser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.ui.module.ModuleScreen
import com.topjohnwu.magisk.ui.module.ModuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuDashboardScreen(
    suViewModel: SuperuserViewModel,
    moduleViewModel: ModuleViewModel
) {
    val tabs = listOf("Apps", "Modules")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("SU Dashboard") }
                )
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) { page ->
            if (page == 0) {
                SuperuserScreen(viewModel = suViewModel, innerPadding = PaddingValues(0.dp))
            } else {
                ModuleScreen(viewModel = moduleViewModel, innerPadding = PaddingValues(0.dp))
            }
        }
    }
}
