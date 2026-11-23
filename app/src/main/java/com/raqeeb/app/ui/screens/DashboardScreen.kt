package com.raqeeb.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
    onNavigateApps: () -> Unit
) {
    val shieldScale by animateFloatAsState(
        targetValue = if (state.isProtectionEnabled) 1.05f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "shieldScale"
    )
    val statusColor by animateColorAsState(
        targetValue = if (state.isProtectionEnabled) AccentGold else AlertRed,
        label = "statusColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. بطاقة حالة الحماية
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DeepEmerald),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(DeepEmerald, EmeraldMedium)))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (state.isProtectionEnabled) Icons.Rounded.VerifiedUser else Icons.Rounded.GppMaybe,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier
                                .size(38.dp)
                                .scale(shieldScale)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (state.isProtectionEnabled) {
                                    if (state.isArabic) "الحماية مُفعّلة" else "Protection Active"
                                } else {
                                    if (state.isArabic) "الحماية متوقفة" else "Protection Paused"
                                },
                                color = SoftWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.isArabic) state.protectionMode.titleAr else state.protectionMode.titleEn,
                                color = AccentGold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Switch(
                        checked = state.isProtectionEnabled,
                        onCheckedChange = onToggleProtection,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepEmerald,
                            checkedTrackColor = AccentGold
                        )
                    )
                }

                Text(
                    text = if (state.isArabic) state.protectionMode.descAr else state.protectionMode.descEn,
                    color = SoftWhite.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // 2. مركز تفعيل الأذونات الفعلية (الإشعارات، الوصول، الاستخدام، VPN)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (state.isArabic) "أذونات الحماية والتحكم الفعلي" else "Active Control Permissions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                PermissionRow(
                    title = if (state.isArabic) "إذن الإشعارات والتذكيرات" else "Notifications Permission",
                    granted = state.permissions.notificationsGranted,
                    isArabic = state.isArabic,
                    onClick = onRequestNotificationPerm
                )
                PermissionRow(
                    title = if (state.isArabic) "خدمة إمكانية الوصول (مراقبة التطبيقات)" else "Accessibility Service",
                    granted = state.permissions.accessibilityGranted,
                    isArabic = state.isArabic,
                    onClick = onRequestAccessibilityPerm
                )
                PermissionRow(
                    title = if (state.isArabic) "إحصائيات الاستخدام (جلب التطبيقات)" else "Usage Access Permission",
                    granted = state.permissions.usageStatsGranted,
                    isArabic = state.isArabic,
                    onClick = onRequestUsageStatsPerm
                )
                PermissionRow(
                    title = if (state.isArabic) "درع حظر المواقع الإباحية (VPN محلي)" else "Adult Sites Blocker (Local VPN)",
                    granted = state.permissions.vpnActive,
                    isArabic = state.isArabic,
                    onClick = onToggleVpn
                )
            }
        }

        // 3. زر الطوارئ الذكي "أنقذني"
        Button(
            onClick = onOpenRescue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
        ) {
            Icon(Icons.Rounded.VolunteerActivism, contentDescription = null, tint = DeepEmerald)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (state.isArabic) "أنقذني — تدخل إيماني فوري" else "Rescue Me — Instant Support",
                color = DeepEmerald,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 4. مستويات الحماية
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProtectionMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.protectionMode == mode,
                    onClick = { onSelectMode(mode) },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            text = if (state.isArabic) mode.titleAr else mode.titleEn,
                            fontSize = 11.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepEmerald,
                        selectedLabelColor = AccentGold
                    )
                )
            }
        }

        // 5. بطاقة الذكر والآية اليومية
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.isArabic) "تذكير إيماني" else "Islamic Reminder",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onNextReminder, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Next", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    text = if (state.isArabic) state.currentReminder.textAr else state.currentReminder.textEn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp
                )
                Text(
                    text = if (state.isArabic) state.currentReminder.sourceAr else state.currentReminder.sourceEn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 6. الوصول السريع
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessCard(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "درع المواقع" else "Sites Shield",
                subtitle = if (state.isArabic) "مُفعّل (+45,000)" else "Active (+45,000)",
                icon = Icons.Rounded.Language,
                onClick = onNavigateWebsites
            )
            QuickAccessCard(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "التطبيقات المقيدة" else "Restricted Apps",
                subtitle = "${state.apps.count { it.isRestricted }} / ${state.apps.size}",
                icon = Icons.Rounded.AppBlocking,
                onClick = onNavigateApps
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    granted: Boolean,
    isArabic: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 13.sp, modifier = Modifier.weight(1f))
        AssistChip(
            onClick = onClick,
            label = {
                Text(
                    text = if (granted) (if (isArabic) "مُفعّل ✓" else "Active ✓")
                    else (if (isArabic) "تفعيل الآن" else "Enable"),
                    fontSize = 11.sp
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (granted) DeepEmerald else AlertRed.copy(alpha = 0.2f),
                labelColor = if (granted) AccentGold else AlertRed
            )
        )
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentGold)
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
