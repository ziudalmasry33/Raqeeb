package com.raqeeb.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raqeeb.app.service.RaqeebVpnService
import com.raqeeb.app.ui.components.RescueDialog
import com.raqeeb.app.ui.screens.*
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald
import com.raqeeb.app.ui.theme.RaqeebTheme
import com.raqeeb.app.ui.theme.SoftWhite
import com.raqeeb.app.ui.viewmodel.MainViewModel
import com.raqeeb.app.util.PermissionHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()
            var selectedTab by remember { mutableIntStateOf(0) }
            var showFirstLaunchUsagePrompt by remember { mutableStateOf(false) }

            val notificationLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                viewModel.refreshSystemState(context, RaqeebVpnService.isRunning)
                if (granted) {
                    PermissionHelper.sendReminderNotification(
                        context,
                        "رقيب جاهز لمرافقتك 🌿",
                        "تم تفعيل الإشعارات الإيمانية بنجاح."
                    )
                }
            }

            val vpnLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val vpnIntent = Intent(context, RaqeebVpnService::class.java)
                    ContextCompat.startForegroundService(context, vpnIntent)
                    viewModel.setVpnActive(true)
                }
            }

            LaunchedEffect(Unit) {
                viewModel.refreshSystemState(context, RaqeebVpnService.isRunning)
                if (!PermissionHelper.hasUsageStatsPermission(context)) {
                    showFirstLaunchUsagePrompt = true
                }
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.refreshSystemState(context, RaqeebVpnService.isRunning)
                        if (PermissionHelper.hasUsageStatsPermission(context)) {
                            showFirstLaunchUsagePrompt = false
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val layoutDirection = if (state.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                RaqeebTheme(darkTheme = state.isDarkMode) {
                    Scaffold(
                        topBar = {
                            @OptIn(ExperimentalMaterial3Api::class)
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(AccentGold.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (state.isArabic) "رقيب" else "RAQEEB",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp
                                        )
                                    }
                                },
                                actions = {
                                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                                        Text(
                                            text = if (state.isArabic) "EN" else "عربي",
                                            color = AccentGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                                        Icon(
                                            imageVector = if (state.isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                            contentDescription = "Theme",
                                            tint = AccentGold
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = DeepEmerald,
                                    titleContentColor = SoftWhite
                                )
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = { (slideInHorizontally(animationSpec = tween(260)) { if (targetState > initialState) it / 4 else -it / 4 } + fadeIn(tween(260))) togetherWith (slideOutHorizontally(animationSpec = tween(220)) { if (targetState > initialState) -it / 4 else it / 4 } + fadeOut(tween(200))) },
                                label = "tabTransition"
                            ) { tab ->
                                when (tab) {
                                    0 -> DashboardScreen(
                                        state = state,
                                        onToggleProtection = viewModel::toggleProtection,
                                        onSelectMode = viewModel::setProtectionMode,
                                        onNextReminder = viewModel::nextReminder,
                                        onOpenRescue = { viewModel.setShowRescueDialog(true) },
                                        onRequestNotificationPerm = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        },
                                        onRequestAccessibilityPerm = {
                                            PermissionHelper.openAccessibilitySettings(context)
                                        },
                                        onRequestUsageStatsPerm = {
                                            PermissionHelper.openUsageAccessSettings(context)
                                        },
                                        onToggleVpn = {
                                            if (state.permissions.vpnActive || RaqeebVpnService.isRunning) {
                                                context.startService(Intent(context, RaqeebVpnService::class.java).apply {
                                                    action = "STOP_VPN"
                                                })
                                                viewModel.setVpnActive(false)
                                            } else {
                                                val vpnIntent = PermissionHelper.prepareVpnIntent(context)
                                                if (vpnIntent != null) {
                                                    vpnLauncher.launch(vpnIntent)
                                                } else {
                                                    val startIntent = Intent(context, RaqeebVpnService::class.java)
                                                    ContextCompat.startForegroundService(context, startIntent)
                                                    viewModel.setVpnActive(true)
                                                }
                                            }
                                        },
                                        onNavigateWebsites = { selectedTab = 1 },
                                        onNavigateApps = { selectedTab = 2 }
                                    )
                                    1 -> WebsitesScreen(
                                        isArabic = state.isArabic,
                                        domains = state.domains,
                                        onAddDomain = viewModel::addCustomDomain,
                                        onToggleDomain = viewModel::toggleDomain,
                                        onRemoveDomain = viewModel::removeDomain
                                    )
                                    2 -> AppsScreen(
                                        isArabic = state.isArabic,
                                        apps = state.apps,
                                        onToggleApp = viewModel::toggleAppRestriction
                                    )
                                    3 -> ChallengesScreen(
                                        isArabic = state.isArabic,
                                        challenges = state.challenges,
                                        onToggleChallenge = viewModel::toggleChallengeToday
                                    )
                                }
                            }

                            // شريط التنقل السفلي العائم العصري (Floating Pill Navigation Bar)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(50),
                                color = DeepEmerald.copy(alpha = 0.96f),
                                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.45f)),
                                shadowElevation = 10.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloatingNavItem(
                                        selected = selectedTab == 0,
                                        label = if (state.isArabic) "الرئيسية" else "Home",
                                        filledIcon = Icons.Rounded.SpaceDashboard,
                                        outlinedIcon = Icons.Outlined.SpaceDashboard,
                                        onClick = { selectedTab = 0 }
                                    )
                                    FloatingNavItem(
                                        selected = selectedTab == 1,
                                        label = if (state.isArabic) "المواقع" else "Sites",
                                        filledIcon = Icons.Rounded.VerifiedUser,
                                        outlinedIcon = Icons.Outlined.VerifiedUser,
                                        onClick = { selectedTab = 1 }
                                    )
                                    FloatingNavItem(
                                        selected = selectedTab == 2,
                                        label = if (state.isArabic) "التطبيقات" else "Apps",
                                        filledIcon = Icons.Rounded.Widgets,
                                        outlinedIcon = Icons.Outlined.Widgets,
                                        onClick = { selectedTab = 2 }
                                    )
                                    FloatingNavItem(
                                        selected = selectedTab == 3,
                                        label = if (state.isArabic) "التحديات" else "Goals",
                                        filledIcon = Icons.Rounded.EmojiEvents,
                                        outlinedIcon = Icons.Outlined.EmojiEvents,
                                        onClick = { selectedTab = 3 }
                                    )
                                }
                            }
                        }

                        if (showFirstLaunchUsagePrompt) {
                            AlertDialog(
                                onDismissRequest = { showFirstLaunchUsagePrompt = false },
                                icon = { Icon(Icons.Rounded.DataUsage, contentDescription = null, tint = AccentGold) },
                                title = {
                                    Text(
                                        text = if (state.isArabic) "تفعيل صلاحية تتبع الاستخدام" else "Enable Usage Access",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                text = {
                                    Text(
                                        text = if (state.isArabic)
                                            "يحتاج تطبيق رقيب لصلاحية الوصول إلى بيانات الاستخدام للتعرف على التطبيقات المثبتة وتذكيرك بعد 5 ثوانٍ من فتح أي تطبيق مقيد. تبقى جميع بياناتك على جهازك فقط."
                                        else
                                            "RAQEEB needs Usage Access permission to detect installed apps and remind you 5 seconds after opening a restricted app. All data stays strictly on your device."
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showFirstLaunchUsagePrompt = false
                                            PermissionHelper.openUsageAccessSettings(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepEmerald),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (state.isArabic) "منح الصلاحية الآن" else "Grant Now", color = AccentGold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showFirstLaunchUsagePrompt = false }) {
                                        Text(if (state.isArabic) "لاحقاً" else "Later")
                                    }
                                }
                            )
                        }

                        if (state.showRescueDialog) {
                            RescueDialog(
                                isArabic = state.isArabic,
                                onDismiss = { viewModel.setShowRescueDialog(false) },
                                onExitToHome = {
                                    viewModel.setShowRescueDialog(false)
                                    startActivity(Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    selected: Boolean,
    label: String,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) AccentGold else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = if (selected) 14.dp else 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (selected) filledIcon else outlinedIcon,
            contentDescription = label,
            tint = if (selected) DeepEmerald else SoftWhite.copy(alpha = 0.75f),
            modifier = Modifier.size(22.dp)
        )
        if (selected) {
            Text(
                text = label,
                color = DeepEmerald,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
