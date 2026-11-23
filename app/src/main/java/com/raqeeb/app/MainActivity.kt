package com.raqeeb.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raqeeb.app.ui.components.RescueDialog
import com.raqeeb.app.ui.screens.*
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald
import com.raqeeb.app.ui.theme.RaqeebTheme
import com.raqeeb.app.ui.theme.SoftWhite
import com.raqeeb.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()
            var selectedTab by remember { mutableIntStateOf(0) }

            val layoutDirection = if (state.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                RaqeebTheme(darkTheme = state.isDarkMode) {
                    Scaffold(
                        topBar = {
                            @OptIn(ExperimentalMaterial3Api::class)
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shield,
                                            contentDescription = null,
                                            tint = AccentGold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (state.isArabic) "رقيب | RAQEEB" else "RAQEEB",
                                            fontWeight = FontWeight.Bold
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
                        },
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Rounded.Dashboard, contentDescription = null) },
                                    label = { Text(if (state.isArabic) "الرئيسية" else "Home") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Rounded.Language, contentDescription = null) },
                                    label = { Text(if (state.isArabic) "المواقع" else "Sites") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Rounded.Apps, contentDescription = null) },
                                    label = { Text(if (state.isArabic) "التطبيقات" else "Apps") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    icon = { Icon(Icons.Rounded.EmojiEvents, contentDescription = null) },
                                    label = { Text(if (state.isArabic) "التحديات" else "Goals") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                0 -> DashboardScreen(
                                    state = state,
                                    onToggleProtection = viewModel::toggleProtection,
                                    onSelectMode = viewModel::setProtectionMode,
                                    onNextReminder = viewModel::nextReminder,
                                    onOpenRescue = { viewModel.setShowRescueDialog(true) },
                                    onNavigateWebsites = { selectedTab = 1 },
                                    onNavigateApps = { selectedTab = 2 }
                                )
                                1 -> WebsitesScreen(
                                    isArabic = state.isArabic,
                                    domains = state.domains,
                                    onAddDomain = viewModel::addDomain,
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

                        if (state.showRescueDialog) {
                            RescueDialog(
                                isArabic = state.isArabic,
                                onDismiss = { viewModel.setShowRescueDialog(false) },
                                onExitToHome = {
                                    viewModel.setShowRescueDialog(false)
                                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    startActivity(homeIntent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
