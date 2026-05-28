package com.topjohnwu.magisk.ui.setup

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.ui.component.SettingsArrow
import com.topjohnwu.magisk.ui.component.SettingsSwitch
import com.topjohnwu.magisk.ui.ThemeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onFinishSetup: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(onClick = onFinishSetup) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> ModuleHubTutorialPage()
                    2 -> QuickSettingsPage()
                }
            }
            
            // Bottom navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishSetup()
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(if (pagerState.currentPage == 2) "Get Started" else "Next")
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }
    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).scale(scale).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Welcome to Magisk Next", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("The next generation of root access, rebuilt for the future with powerful new tools and deep customization.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(48.dp))
        FeatureRow(Icons.Default.Security, "Native Stealth", "Deep system isolation and built-in bootloop protection.")
        Spacer(modifier = Modifier.height(24.dp))
        FeatureRow(Icons.Outlined.CheckCircle, "Advanced Labs", "Experimental features and developer workflow tools.")
    }
}

@Composable
private fun ModuleHubTutorialPage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("The Module Hub", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Discover, download, and update community modules directly from within Magisk Next.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How it works:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("1. Browse the community catalog in the Modules tab.")
                Spacer(Modifier.height(4.dp))
                Text("2. Search or sort to find your favorite modules.")
                Spacer(Modifier.height(4.dp))
                Text("3. Tap the download icon to seamlessly flash it in the background!")
            }
        }
    }
}

@Composable
private fun QuickSettingsPage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
    ) {
        Text("Quick Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 32.dp, bottom = 16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            var checkUpdate by remember { mutableStateOf(Config.checkUpdate) }
            SettingsSwitch("Check Updates", "Automatically check for Magisk Next updates", checkUpdate) { 
                checkUpdate = it; Config.checkUpdate = it 
            }
            
            val context = androidx.compose.ui.platform.LocalContext.current
            val scope = rememberCoroutineScope()
            SettingsArrow("Systemless Hosts", "Pre-setup systemless hosts module for adblockers", onClick = {
                scope.launch { com.topjohnwu.magisk.core.utils.RootUtils.addSystemlessHosts() }
                android.widget.Toast.makeText(context, "Systemless hosts enabled", android.widget.Toast.LENGTH_SHORT).show()
            })

            var zygisk by remember { mutableStateOf(Config.zygisk) }
            SettingsSwitch("Zygisk", "Run code in the processes of all Android applications", zygisk) {
                zygisk = it; Config.zygisk = it
            }

            var denylist by remember { mutableStateOf(Config.suRestrict) }
            SettingsSwitch("Enforce DenyList", "Hide Magisk from specific apps", denylist) {
                denylist = it; Config.suRestrict = it
            }

            var colorMode by remember { mutableIntStateOf(Config.colorMode) }
            SettingsSwitch("Dark Theme", "Enable dark UI mode", colorMode != 1) { 
                val newMode = if (it) 2 else 1
                colorMode = newMode
                Config.colorMode = newMode
                ThemeState.colorMode = newMode
            }
            
            SettingsArrow("Language", "Change app language", onClick = {
                android.widget.Toast.makeText(context, "Language can be changed in main settings", android.widget.Toast.LENGTH_SHORT).show()
            })
            
            SettingsArrow("Download Path", "Change default download directory", onClick = {
                android.widget.Toast.makeText(context, "Download path can be configured in main settings", android.widget.Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
