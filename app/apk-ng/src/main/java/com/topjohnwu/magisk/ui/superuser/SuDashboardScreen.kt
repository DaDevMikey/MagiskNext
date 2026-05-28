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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.ui.module.ModuleScreen
import com.topjohnwu.magisk.ui.module.ModuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuDashboardScreen(
    suViewModel: SuperuserViewModel,
    moduleViewModel: ModuleViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Apps", "Modules")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("SU Dashboard") }
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTabIndex == 0) {
                SuperuserScreen(viewModel = suViewModel, innerPadding = PaddingValues(0.dp))
            } else {
                ModuleScreen(viewModel = moduleViewModel, innerPadding = PaddingValues(0.dp))
            }
        }
    }
}
