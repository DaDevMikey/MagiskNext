package com.topjohnwu.magisk.ui.experimental

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.ui.navigation.Navigator
import com.topjohnwu.magisk.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalFeaturesScreen(navigator: Navigator, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Experimental") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Labs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "These features are under active development and may be unstable.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            ExperimentalFeatureCard(
                icon = Icons.Outlined.Hub,
                title = "Module Hub",
                description = "Browse and download community modules online.",
                onClick = { navigator.push(Route.ModuleHub) }
            )

            ExperimentalFeatureCard(
                icon = Icons.Outlined.Build,
                title = "Integrated Module Creator",
                description = "Create simple modules directly from your device.",
                onClick = { navigator.push(Route.ModuleCreator) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperimentalFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTonalIconButton(onClick = onClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
