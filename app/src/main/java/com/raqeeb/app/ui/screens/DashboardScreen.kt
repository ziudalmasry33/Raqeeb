package com.raqeeb.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raqeeb.app.domain.model.ProtectionMode
import com.raqeeb.app.ui.theme.*
import com.raqeeb.app.ui.viewmodel.RaqeebUiState

@Composable
fun DashboardScreen(
    state: RaqeebUiState,
    onToggleProtection: (Boolean) -> Unit,
    onSelectMode: (ProtectionMode) -> Unit,
    onNextReminder: () -> Unit,
    onOpenRescue: () -> Unit,
    onRequestNotificationPerm: () -> Unit,
    onRequestAccessibilityPerm: () -> Unit,
    onRequestUsageStatsPerm: () -> Unit,
    onToggleVpn: () -> Unit,
    onNavigateWebsites: () -> Unit,
    onNavigateApps: () -> Unit,
    onDismissAdminMsg: () -> Unit = {},
    onSyncCloud: () -> Unit = {},
    onSaveCloudKeys: (String, String) -> Unit = { _, _ -> }
) {
    var showCloudDialog by remember { mutableStateOf(false) }

    val orbScale by animateFloatAsState(
        targetValue = if (state.isProtectionEnabled) 1f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "orbScale"
    )
    val ringColor by animateColorAsState(
        targetValue = if (state.isProtectionEnabled) AccentGold else AlertRed,
        label = "ringColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. بطاقة رسالة الإدارة الفورية (تظهر فور وصول رسالة من لوحة الأدمن)
        state.latestAdminMessage?.let { adminMsg ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                border = BorderStroke(1.5.dp, AccentGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Campaign, contentDescription = null, tint = AccentGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = adminMsg.title,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(onClick = onDismissAdminMsg, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = SoftWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = adminMsg.body,
                        color = SoftWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 1. مركز الحماية الدائري العصري
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(DeepEmerald, Color(0xFF08221A))))
                .border(1.dp, ringColor.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .scale(orbScale)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(ringColor.copy(alpha = 0.25f), Color.Transparent)))
                        .border(2.dp, ringColor, CircleShape)
                        .clickable { onToggleProtection(!state.isProtectionEnabled) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (state.isProtectionEnabled) Icons.Rounded.Shield else Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = ringColor,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.isProtectionEnabled) (if (state.isArabic) "نَشِط" else "ON")
                            else (if (state.isArabic) "متوقف" else "OFF"),
                            color = SoftWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (state.isProtectionEnabled) {
                        if (state.isArabic) "أنت في كنف الحماية والرعاية" else "Digital Sanctuary Active"
                    } else {
                        if (state.isArabic) "اضغط على الدرع لتفعيل الحماية" else "Tap Shield to Activate"
                    },
                    color = SoftWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProtectionMode.entries.forEach { mode ->
                        val selected = state.protectionMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) AccentGold else Color.Transparent)
                                .clickable { onSelectMode(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.isArabic) mode.titleAr else mode.titleEn,
                                color = if (selected) DeepEmerald else SoftWhite.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }

                Text(
                    text = if (state.isArabic) state.protectionMode.descAr else state.protectionMode.descEn,
                    color = SoftWhite.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. زر "لحظة سَكينة"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(AccentGold, Color(0xFFE6C65C))))
                .clickable { onOpenRescue() }
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DeepEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.SelfImprovement, contentDescription = null, tint = DeepEmerald)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (state.isArabic) "لحظة سَكينة" else "Moment of Serenity",
                            color = DeepEmerald,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (state.isArabic) "آيات قرآنية وأدعية تهدئ القلب" else "Quranic verses & peace for your heart",
                            color = DeepEmerald.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (state.isArabic) Icons.Rounded.ArrowBackIosNew else Icons.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint = DeepEmerald,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 3. بطاقة الإشراقة الإيمانية + زر المزامنة مع لوحة الإدارة
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isArabic) "إشراقة إيمانية" else "Daily Reflection",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Row {
                        IconButton(onClick = { showCloudDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.CloudSync, contentDescription = "Cloud Sync", tint = AccentGold, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onNextReminder, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Next", tint = AccentGold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Text(
                    text = if (state.isArabic) state.currentReminder.textAr else state.currentReminder.textEn,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp
                )
                Text(
                    text = if (state.isArabic) state.currentReminder.sourceAr else state.currentReminder.sourceEn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. وحدات الوقاية الذكية
        Text(
            text = if (state.isArabic) "وحدات الوقاية الذكية" else "Smart Guard Modules",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernControlTile(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "درع التصفح (VPN)" else "Safe DNS (VPN)",
                statusText = if (state.permissions.vpnActive) (if (state.isArabic) "سريع ونشط" else "Fast & Active") else (if (state.isArabic) "اضغط للتفعيل" else "Tap to Start"),
                icon = Icons.Rounded.VpnLock,
                isActive = state.permissions.vpnActive,
                onClick = onToggleVpn
            )
            ModernControlTile(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "مراقب التطبيقات" else "App Monitor",
                statusText = if (state.permissions.accessibilityGranted) (if (state.isArabic) "مُفعّل" else "Enabled") else (if (state.isArabic) "تفعيل الخدمة" else "Enable Service"),
                icon = Icons.Rounded.AccessibilityNew,
                isActive = state.permissions.accessibilityGranted,
                onClick = onRequestAccessibilityPerm
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernControlTile(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "تتبع الاستخدام" else "Usage Access",
                statusText = if (state.permissions.usageStatsGranted) (if (state.isArabic) "مُصرّح به" else "Granted") else (if (state.isArabic) "منح الإذن" else "Grant Access"),
                icon = Icons.Rounded.DataUsage,
                isActive = state.permissions.usageStatsGranted,
                onClick = onRequestUsageStatsPerm
            )
            ModernControlTile(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "تنبيهات 5 ثوانٍ" else "5s Reminders",
                statusText = if (state.permissions.notificationsGranted) (if (state.isArabic) "جاهزة" else "Ready") else (if (state.isArabic) "تفعيل الإشعار" else "Allow Alerts"),
                icon = Icons.Rounded.NotificationsActive,
                isActive = state.permissions.notificationsGranted,
                onClick = onRequestNotificationPerm
            )
        }

        Spacer(modifier = Modifier.height(76.dp))
    }

    if (showCloudDialog) {
        var urlInput by remember { mutableStateOf("") }
        var keyInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCloudDialog = false },
            title = { Text(if (state.isArabic) "ربط استقبال رسائل الأدمن" else "Connect Admin Broadcasts", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (state.isArabic) "أدخل بيانات مشروع Supabase لاستقبال رسائل وتذكيرات لوحة الإدارة فوراً:"
                        else "Enter Supabase credentials to receive admin broadcasts:",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, label = { Text("Project URL") }, singleLine = true)
                    OutlinedTextField(value = keyInput, onValueChange = { keyInput = it }, label = { Text("Anon Public Key") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (urlInput.isNotBlank() && keyInput.isNotBlank()) {
                            onSaveCloudKeys(urlInput, keyInput)
                        } else {
                            onSyncCloud()
                        }
                        showCloudDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepEmerald)
                ) {
                    Text(if (state.isArabic) "حفظ وتحديث الآن" else "Save & Sync", color = AccentGold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudDialog = false }) {
                    Text(if (state.isArabic) "إغلاق" else "Close")
                }
            }
        )
    }
}

@Composable
private fun ModernControlTile(
    modifier: Modifier = Modifier,
    title: String,
    statusText: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) DeepEmerald else MaterialTheme.colorScheme.surfaceVariant,
        label = "tileBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) AccentGold else Color.Gray.copy(alpha = 0.2f),
        label = "tileBorder"
    )

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isActive) AccentGold.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isActive) AccentGold else Color.Gray.copy(alpha = 0.4f))
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) SoftWhite else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                fontSize = 11.sp,
                color = if (isActive) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
