package com.topjohnwu.magisk.ui.home

import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.core.BuildConfig
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.download.DownloadEngine
import com.topjohnwu.magisk.core.download.Subject
import com.topjohnwu.magisk.core.ktx.reboot
import com.topjohnwu.magisk.core.ktx.toast
import com.topjohnwu.magisk.core.tasks.AppMigration
import com.topjohnwu.magisk.core.tasks.MagiskInstaller
import com.topjohnwu.magisk.ui.MainActivity
import com.topjohnwu.magisk.ui.component.LoadingDialogHandle
import com.topjohnwu.magisk.ui.component.MarkdownTextAsync
import com.topjohnwu.magisk.ui.component.rememberLoadingDialog
import com.topjohnwu.magisk.ui.flash.FlashUtils
import com.topjohnwu.magisk.ui.install.InstallBottomSheet
import com.topjohnwu.magisk.ui.install.InstallViewModel
import kotlinx.coroutines.launch
import com.topjohnwu.magisk.core.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, installVm: InstallViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as MainActivity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()

    val showUninstallDialog = rememberSaveable { mutableStateOf(false) }
    val showManagerDialog = rememberSaveable { mutableStateOf(false) }
    val showEnvFixDialog = rememberSaveable { mutableStateOf(false) }
    var showHideDialog by rememberSaveable { mutableStateOf(false) }
    var showRestoreDialog by rememberSaveable { mutableStateOf(false) }
    val showInstallSheet = rememberSaveable { mutableStateOf(false) }
    var envFixCode by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.showUninstall) {
        if (uiState.showUninstall) {
            showUninstallDialog.value = true
            viewModel.onUninstallConsumed()
        }
    }
    LaunchedEffect(uiState.showManagerInstall) {
        if (uiState.showManagerInstall) {
            showManagerDialog.value = true
            viewModel.onManagerInstallConsumed()
        }
    }
    LaunchedEffect(uiState.envFixCode) {
        if (uiState.envFixCode != 0) {
            envFixCode = uiState.envFixCode
            showEnvFixDialog.value = true
            viewModel.onEnvFixConsumed()
        }
    }
    LaunchedEffect(uiState.showHideRestore) {
        if (uiState.showHideRestore) {
            val hidden = context.packageName != BuildConfig.APP_PACKAGE_NAME
            if (hidden) showRestoreDialog = true else showHideDialog = true
            viewModel.onHideRestoreConsumed()
        }
    }

    if (showUninstallDialog.value) {
        UninstallComposableDialog(
            showDialog = showUninstallDialog,
            activity = activity,
            loadingDialog = loadingDialog,
        )
    }

    if (showManagerDialog.value) {
        ManagerInstallComposableDialog(
            showDialog = showManagerDialog,
            activity = activity,
        )
    }

    if (showEnvFixDialog.value) {
        EnvFixComposableDialog(
            showDialog = showEnvFixDialog,
            code = envFixCode,
            activity = activity,
            loadingDialog = loadingDialog,
            onNavigateInstall = { showInstallSheet.value = true },
        )
    }

    if (showHideDialog) {
        HideAppDialog(
            onDismiss = { showHideDialog = false },
            onConfirm = { name ->
                showHideDialog = false
                scope.launch {
                    loadingDialog.withLoading {
                        AppMigration.patchAndHide(context, name)
                    }
                }
            }
        )
    }

    if (showRestoreDialog) {
        RestoreAppDialog(
            onDismiss = { showRestoreDialog = false },
            onConfirm = {
                showRestoreDialog = false
                scope.launch {
                    loadingDialog.withLoading {
                        AppMigration.restoreApp(context)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreR.string.section_home)) },
                scrollBehavior = scrollBehavior,
                actions = {
                    if (Info.isRooted) {
                        RebootButton()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isNoticeVisible) {
                NoticeCard(onHide = viewModel::hideNotice)
            }

            MagiskNextStatusCard(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.magiskState,
                version = viewModel.magiskInstalledVersion,
                onInstallClicked = { showInstallSheet.value = true }
            )

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                state = uiState.appState,
                version = viewModel.managerInstalledVersion,
                remoteVersion = uiState.managerRemoteVersion,
                progress = uiState.managerProgress,
                isHidden = context.packageName != BuildConfig.APP_PACKAGE_NAME,
                onManagerPressed = viewModel::onManagerPressed,
                onHideRestorePressed = viewModel::onHideRestorePressed,
            )

            UninstallButton(
                onClick = { viewModel.onDeletePressed() },
                enabled = Info.env.isActive
            )

            Text(
                text = "Source Code",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            SourceCodeCard(onLinkClicked = viewModel::onLinkPressed)
        }
    }

    InstallBottomSheet(
        show = showInstallSheet,
        installVm = installVm,
    )
}

@Composable
private fun RebootButton() {
    val showMenu = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var safeModeEnabled by remember { mutableIntStateOf(Config.bootloop) }

    val showUserspace = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        context.getSystemService<PowerManager>()?.isRebootingUserspaceSupported == true
    val showSafeMode = Const.Version.atLeast_28_0()

    val items = buildList {
        add(RebootOption(CoreR.string.reboot) { reboot() })
        if (showUserspace) {
            add(RebootOption(CoreR.string.reboot_userspace) { reboot("userspace") })
        }
        add(RebootOption(CoreR.string.reboot_recovery) { reboot("recovery") })
        add(RebootOption(CoreR.string.reboot_bootloader) { reboot("bootloader") })
        add(RebootOption(CoreR.string.reboot_download) { reboot("download") })
        add(RebootOption(CoreR.string.reboot_edl) { reboot("edl") })
        if (showSafeMode) {
            add(RebootOption(CoreR.string.reboot_safe_mode) {
                val newVal = if (safeModeEnabled >= 2) 0 else 2
                Config.bootloop = newVal
                safeModeEnabled = newVal
            })
        }
    }

    Box {
        IconButton(
            modifier = Modifier.padding(end = 16.dp),
            onClick = { showMenu.value = true },
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = stringResource(CoreR.string.reboot),
            )
        }
        DropdownMenu(
            expanded = showMenu.value,
            onDismissRequest = { showMenu.value = false }
        ) {
            items.forEachIndexed { index, item ->
                val isSafeMode = item.labelRes == CoreR.string.reboot_safe_mode
                DropdownMenuItem(
                    text = { Text(stringResource(item.labelRes)) },
                    trailingIcon = if (isSafeMode && safeModeEnabled >= 2) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                    onClick = {
                        item.action()
                        if (!isSafeMode) showMenu.value = false
                    }
                )
            }
        }
    }
}

private class RebootOption(val labelRes: Int, val action: () -> Unit)

@Composable
private fun NoticeCard(onHide: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                RoundedCornerShape(16.dp)
            )
            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(CoreR.string.home_notice_content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
            IconButton(onClick = onHide) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(CoreR.string.hide),
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun InstallButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_download),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MagiskNextStatusCard(
    modifier: Modifier = Modifier,
    state: HomeViewModel.State,
    version: String,
    onInstallClicked: () -> Unit,
) {
    val actualState = if (Config.fakeRoot) HomeViewModel.State.UP_TO_DATE else state
    val actualVersion = if (Config.fakeRoot) com.topjohnwu.magisk.core.BuildConfig.APP_VERSION_NAME else version

    val actionLabel = when (actualState) {
        HomeViewModel.State.OUTDATED -> stringResource(CoreR.string.update)
        HomeViewModel.State.INVALID -> stringResource(CoreR.string.install)
        HomeViewModel.State.UP_TO_DATE -> stringResource(CoreR.string.reinstall)
        HomeViewModel.State.LOADING -> null
    }

    val isWorking = actualState == HomeViewModel.State.UP_TO_DATE || actualState == HomeViewModel.State.OUTDATED
    val cardColor = if (isWorking) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val onCardColor = if (isWorking) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    val iconRes = if (isWorking) Icons.Default.Check else Icons.Default.Close

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconRes,
                            contentDescription = null,
                            tint = onCardColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isWorking) "Magisk Next is Working" else "Magisk Next Not Installed",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = onCardColor
                        )
                        Text(
                            text = "Version: " + actualVersion.ifEmpty { stringResource(CoreR.string.not_available) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = onCardColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusItem(
                    label = stringResource(CoreR.string.zygisk),
                    value = stringResource(if (Info.isZygiskEnabled || Config.fakeRoot) CoreR.string.enabled else CoreR.string.disabled),
                    color = onCardColor
                )
                VerticalDivider(modifier = Modifier.height(32.dp), color = onCardColor.copy(alpha = 0.2f))
                StatusItem(
                    label = stringResource(CoreR.string.ramdisk),
                    value = stringResource(if (Info.ramdisk || Config.fakeRoot) CoreR.string.yes else CoreR.string.no),
                    color = onCardColor
                )
            }

            if (actionLabel != null) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onInstallClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(actionLabel, color = cardColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = color)
    }
}

@Composable
private fun UninstallButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled || Config.fakeRoot,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(CoreR.string.uninstall_magisk_title),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun AppCard(
    modifier: Modifier = Modifier,
    state: HomeViewModel.State,
    version: String,
    remoteVersion: String,
    progress: Int,
    isHidden: Boolean,
    onManagerPressed: () -> Unit,
    onHideRestorePressed: () -> Unit,
) {
    val actionLabel = when (state) {
        HomeViewModel.State.OUTDATED -> stringResource(CoreR.string.update)
        HomeViewModel.State.UP_TO_DATE -> stringResource(CoreR.string.reinstall)
        else -> null
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_manager),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(CoreR.string.home_app_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (Info.env.isActive || Config.fakeRoot) {
                    val hideRestoreIcon = if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onHideRestorePressed) {
                        Icon(
                            imageVector = hideRestoreIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                if (actionLabel != null) {
                    InstallButton(
                        label = actionLabel,
                        onClick = onManagerPressed,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state != HomeViewModel.State.LOADING) {
                AppDetailRow(label = stringResource(CoreR.string.home_latest_version), value = remoteVersion)
            }
            AppDetailRow(label = stringResource(CoreR.string.home_installed_version), value = version)
            AppDetailRow(label = stringResource(CoreR.string.home_package), value = LocalContext.current.packageName)

            if (progress in 1..99) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AppDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SourceCodeCard(onLinkClicked: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Magisk Next is an open source project. View the source code for both Magisk Next and Stock Magisk below.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onLinkClicked("https://github.com/DaDevMikey/MagiskNext") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(painterResource(CoreR.drawable.ic_github), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("MagiskNext")
                }
                Button(
                    onClick = { onLinkClicked("https://github.com/topjohnwu/Magisk") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(painterResource(CoreR.drawable.ic_github), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Stock Magisk")
                }
            }
        }
    }
}

@Composable
private fun UninstallComposableDialog(
    showDialog: MutableState<Boolean>,
    activity: MainActivity,
    loadingDialog: LoadingDialogHandle,
) {
    val scope = rememberCoroutineScope()
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(CoreR.string.uninstall_magisk_title)) },
            text = {
                Text(
                    text = stringResource(CoreR.string.uninstall_magisk_msg),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        val intent = Intent(activity, activity.javaClass).apply {
                            action = FlashUtils.INTENT_FLASH
                            putExtra(FlashUtils.EXTRA_FLASH_ACTION, Const.Value.UNINSTALL)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        activity.startActivity(intent)
                    }
                ) {
                    Text(stringResource(CoreR.string.complete_uninstall))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        scope.launch {
                            val success = loadingDialog.withLoading {
                                MagiskInstaller.Restore().exec()
                            }
                            activity.toast(
                                if (success) CoreR.string.restore_done else CoreR.string.restore_fail,
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                ) {
                    Text(stringResource(CoreR.string.restore_img))
                }
            }
        )
    }
}

@Composable
private fun ManagerInstallComposableDialog(
    showDialog: MutableState<Boolean>,
    activity: MainActivity,
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(CoreR.string.install)) },
            text = {
                MarkdownTextAsync {
                    val text = Info.update.note
                    java.io.File(activity.cacheDir, "${Info.update.versionCode}.md").writeText(text)
                    text
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        DownloadEngine.startWithActivity(activity, Subject.App())
                    }
                ) {
                    Text(stringResource(CoreR.string.install))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EnvFixComposableDialog(
    showDialog: MutableState<Boolean>,
    code: Int,
    activity: MainActivity,
    loadingDialog: LoadingDialogHandle,
    onNavigateInstall: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val needsFullFix = code == 2 ||
        Info.env.versionCode != com.topjohnwu.magisk.core.BuildConfig.APP_VERSION_CODE ||
        Info.env.versionString != com.topjohnwu.magisk.core.BuildConfig.APP_VERSION_NAME

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(CoreR.string.env_fix_title)) },
            text = {
                Text(
                    text = stringResource(
                        if (needsFullFix) CoreR.string.env_full_fix_msg else CoreR.string.env_fix_msg
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        if (needsFullFix) {
                            onNavigateInstall()
                        } else {
                            scope.launch {
                                val success = loadingDialog.withLoading {
                                    MagiskInstaller.FixEnv().exec()
                                }
                                activity.toast(
                                    if (success) CoreR.string.reboot_delay_toast else CoreR.string.setup_fail,
                                    Toast.LENGTH_LONG
                                )
                                if (success) {
                                    @Suppress("DEPRECATION")
                                    android.os.Handler(android.os.Looper.getMainLooper())
                                        .postDelayed({ reboot() }, 5000)
                                }
                            }
                        }
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun HideAppDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val defaultName = stringResource(CoreR.string.settings)
    var appName by rememberSaveable { mutableStateOf(defaultName) }
    val isError = appName.length > AppMigration.MAX_LABEL_LENGTH || appName.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CoreR.string.settings_hide_app_title)) },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(CoreR.string.settings_app_name_hint)) },
                    isError = isError
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (!isError) onConfirm(appName) },
                enabled = !isError
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun RestoreAppDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CoreR.string.settings_restore_app_title)) },
        text = {
            Text(
                text = stringResource(CoreR.string.restore_app_confirmation),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
