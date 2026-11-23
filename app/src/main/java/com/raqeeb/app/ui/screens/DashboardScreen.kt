package com.raqeeb.app.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onNavigateWebsites: () -> Unit,
    onNavigateApps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DeepEmerald),
            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(DeepEmerald, EmeraldMedium)))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            tint = if (state.isProtectionEnabled) AccentGold else AlertRed,
                            modifier = Modifier.size(36.dp)
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
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Button(
            onClick = onOpenRescue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
        ) {
            Icon(
                imageVector = Icons.Rounded.VolunteerActivism,
                contentDescription = null,
                tint = DeepEmerald
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (state.isArabic) "أنقذني — تدخل إيماني فوري" else "Rescue Me — Instant Support",
                color = DeepEmerald,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = if (state.isArabic) "مستوى الحماية" else "Protection Level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProtectionMode.entries.forEach { mode ->
                val selected = state.protectionMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { onSelectMode(mode) },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            text = if (state.isArabic) mode.titleAr else mode.titleEn,
                            fontSize = 12.sp,
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

        Text(
            text = if (state.isArabic) "ملخص اليوم" else "Today's Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "تذكيرات ظهرت" else "Reminders Shown",
                value = state.remindersShownToday.toString(),
                icon = Icons.Rounded.NotificationsActive
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "محاولات تم صدّها" else "Attempts Blocked",
                value = state.blockedAttemptsToday.toString(),
                icon = Icons.Rounded.Block
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Next Reminder",
                            tint = MaterialTheme.colorScheme.primary
                        )
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessCard(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "المواقع المحظورة" else "Blocked Sites",
                subtitle = "${state.domains.count { it.isEnabled }}",
                icon = Icons.Rounded.Language,
                onClick = onNavigateWebsites
            )
            QuickAccessCard(
                modifier = Modifier.weight(1f),
                title = if (state.isArabic) "التطبيقات المقيدة" else "Restricted Apps",
                subtitle = "${state.apps.count { it.isRestricted }}",
                icon = Icons.Rounded.AppBlocking,
                onClick = onNavigateApps
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentGold)
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
