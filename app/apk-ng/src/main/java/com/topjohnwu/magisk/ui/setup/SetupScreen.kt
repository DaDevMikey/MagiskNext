package com.topjohnwu.magisk.ui.setup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.ui.component.SettingsSwitch
import com.topjohnwu.magisk.ui.ThemeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SetupPageData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun SetupScreen(onFinishSetup: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var revealProgress by remember { mutableFloatStateOf(0f) }
    
    val pages = remember {
        listOf(
            SetupPageData(
                icon = Icons.Default.RocketLaunch,
                title = "Welcome to Magisk Next",
                subtitle = "The next generation of root access, rebuilt for the future with powerful new tools and deep customization.",
                content = { WelcomeMockup() }
            ),
            SetupPageData(
                icon = Icons.Default.AutoAwesome,
                title = "The Module Hub",
                subtitle = "Discover, download, and update community modules directly from within Magisk Next.",
                content = { ModuleHubMockup() }
            ),
            SetupPageData(
                icon = Icons.Outlined.Science,
                title = "Experimental Labs",
                subtitle = "Opt-in to cutting edge tools like the new Ultimate Toolbox in Settings > Experimental.",
                content = { LabsMockup() }
            ),
            SetupPageData(
                icon = Icons.Default.Person,
                title = "Choose Your Profile",
                subtitle = "We can pre-configure Magisk Next based on how you plan to use it.",
                content = { ProfileSelector() }
            ),
            SetupPageData(
                icon = Icons.Default.Memory,
                title = "Zygisk",
                subtitle = "Run code in the processes of all Android applications. Required by many advanced modules.",
                content = { ZygiskSetting() }
            ),
            SetupPageData(
                icon = Icons.Default.Security,
                title = "DenyList",
                subtitle = "Hide the Magisk su binary from specific apps to bypass detection.",
                content = { DenyListSetting() }
            ),
            SetupPageData(
                icon = Icons.Default.DarkMode,
                title = "Appearance",
                subtitle = "Choose between Light and Dark mode for the app UI.",
                content = { ThemeSetting() }
            ),
            SetupPageData(
                icon = Icons.Default.CheckCircle,
                title = "All Set!",
                subtitle = "You are ready to experience the next generation of root.",
                content = { FinishMockup() }
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPageData = pages[pagerState.currentPage]

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val maxRadius = kotlin.math.hypot(screenWidthPx, screenHeightPx)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (revealProgress > 0f) {
                    val radius = maxRadius * revealProgress
                    val path = Path().apply {
                        addRect(Rect(0f, 0f, size.width, size.height))
                        val centerX = size.width - 48.dp.toPx() // FAB X approx
                        val centerY = size.height - 48.dp.toPx() // FAB Y approx
                        addOval(Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius))
                        fillType = PathFillType.EvenOdd
                    }
                    clip = true
                    shape = object : Shape {
                        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) = Outline.Generic(path)
                    }
                }
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar with Skip
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = { 
                        coroutineScope.launch {
                            pagerState.scrollToPage(pages.size - 1)
                        }
                    }) {
                        Text("Skip")
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Animated Header Icon
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPageData.icon,
                    transitionSpec = {
                        if (targetState == Icons.Default.CheckCircle) {
                            (scaleIn(initialScale = 0.5f) + fadeIn()).togetherWith(scaleOut(targetScale = 0.5f) + fadeOut())
                        } else {
                            (slideInVertically { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        }
                    },
                    label = "icon_transition"
                ) { icon ->
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Animated Titles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = currentPageData.title,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300, delayMillis = 100))).togetherWith(fadeOut(animationSpec = tween(150)))
                    },
                    label = "title"
                ) { title ->
                    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedContent(
                    targetState = currentPageData.subtitle,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300, delayMillis = 150))).togetherWith(fadeOut(animationSpec = tween(150)))
                    },
                    label = "subtitle"
                ) { subtitle ->
                    Text(subtitle, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    pages[page].content()
                }
            }

            // Bottom Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { index ->
                        val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                
                FloatingActionButton(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // Trigger reveal animation
                            coroutineScope.launch {
                                animate(0f, 1f, animationSpec = tween(600, easing = FastOutSlowInEasing)) { value, _ ->
                                    revealProgress = value
                                }
                                onFinishSetup()
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (pagerState.currentPage == pages.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeMockup() {
    Card(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Security, null, modifier = Modifier.size(64.dp).align(Alignment.Center).graphicsLayer { alpha = 0.2f })
            Text("Ready to dive in.", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ModuleHubMockup() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer))
                Spacer(Modifier.width(12.dp))
                Column {
                    Box(modifier = Modifier.width(120.dp).height(12.dp).background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.width(80.dp).height(10.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(4.dp)))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer))
                Spacer(Modifier.width(12.dp))
                Column {
                    Box(modifier = Modifier.width(100.dp).height(12.dp).background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.width(60.dp).height(10.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
private fun ProfileSelector() {
    var selectedProfile by remember { mutableStateOf(0) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileCard(
            title = "Standard User",
            subtitle = "Default settings. Best for everyday users.",
            selected = selectedProfile == 0,
            onClick = { selectedProfile = 0; Config.checkUpdate = true; Config.zygisk = false }
        )
        ProfileCard(
            title = "Developer",
            subtitle = "Enables Zygisk and developer logging by default.",
            selected = selectedProfile == 1,
            onClick = { selectedProfile = 1; Config.zygisk = true }
        )
        ProfileCard(
            title = "Module Creator",
            subtitle = "Enables all advanced labs and testing features.",
            selected = selectedProfile == 2,
            onClick = { selectedProfile = 2; Config.zygisk = true }
        )
    }
}

@Composable
private fun ProfileCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ZygiskSetting() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            var zygisk by remember { mutableStateOf(Config.zygisk) }
            SettingsSwitch("Enable Zygisk", "Zygisk runs code in the zygote daemon.", zygisk) {
                zygisk = it; Config.zygisk = it
            }
        }
    }
}

@Composable
private fun DenyListSetting() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            var denylist by remember { mutableStateOf(Config.suRestrict) }
            SettingsSwitch("Enforce DenyList", "Strictly hide Magisk from selected apps.", denylist) {
                denylist = it; Config.suRestrict = it
            }
        }
    }
}

@Composable
private fun ThemeSetting() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            var colorMode by remember { mutableIntStateOf(Config.colorMode) }
            SettingsSwitch("Dark Theme", "Toggle dark UI mode.", colorMode != 1) { 
                val newMode = if (it) 2 else 1
                colorMode = newMode
                Config.colorMode = newMode
                ThemeState.colorMode = newMode
            }
            
            // Visual Preview Mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (colorMode != 1) Color(0xFF1E1E1E) else Color(0xFFF3F3F3)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Android, null, tint = if (colorMode != 1) Color.White else Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Magisk Next", color = if (colorMode != 1) Color.White else Color.Black, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun LabsMockup() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Experimental Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Once you finish setup, head over to Settings -> Experimental to unlock bleeding-edge tools like the Ultimate Toolbox, built-in App Downgrader, and more!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FinishMockup() {
    Card(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(64.dp).align(Alignment.Center).graphicsLayer { alpha = 0.2f })
            Text("Tap the checkmark below to launch.", style = MaterialTheme.typography.titleMedium)
        }
    }
}
