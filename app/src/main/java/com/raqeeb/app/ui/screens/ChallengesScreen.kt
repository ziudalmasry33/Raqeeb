package com.raqeeb.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raqeeb.app.domain.model.DailyChallenge
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald
import com.raqeeb.app.ui.theme.SoftWhite

@Composable
fun ChallengesScreen(
    isArabic: Boolean,
    challenges: List<DailyChallenge>,
    onCompleteChallenge: (Int) -> Unit,
    onAddChallenge: (String, Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val completedTodayCount = challenges.count { it.isCompletedToday }
    val totalDaysAccumulated = challenges.sumOf { it.completedDays }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 86.dp)
    ) {
        // 1. بطاقة ملخص الإنجاز والالتزام العلوي
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(DeepEmerald, Color(0xFF0A3D2E))))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "لوحة الاستقامة والهمّة" else "Consistency Tracker",
                                color = AccentGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "أنجزت اليوم $completedTodayCount من ${challenges.size} تحديات"
                                else "Completed $completedTodayCount of ${challenges.size} today",
                                color = SoftWhite.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                        FilledTonalButton(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AccentGold,
                                contentColor = DeepEmerald
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "تحدٍّ جديد" else "New Goal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            value = "$totalDaysAccumulated",
                            label = if (isArabic) "مجموع أيام الالتزام" else "Total Days"
                        )
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            value = "${if (challenges.isEmpty()) 0 else (completedTodayCount * 100 / challenges.size)}%",
                            label = if (isArabic) "إنجاز اليوم" else "Today's Rate"
                        )
                    }
                }
            }
        }

        // 2. قائمة بطاقات التحديات المنظمة
        items(challenges, key = { it.id }) { challenge ->
            OrganizedChallengeCard(
                isArabic = isArabic,
                challenge = challenge,
                onComplete = { onCompleteChallenge(challenge.id) }
            )
        }
    }

    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var targetDays by remember { mutableStateOf("7") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (isArabic) "إضافة تحدٍّ إيماني جديد" else "Add New Challenge", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(if (isArabic) "عنوان التحدي (مثال: صلاة الفجر)" else "Challenge Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetDays,
                        onValueChange = { targetDays = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isArabic) "عدد الأيام المستهدف (مثال: 7 أو 30)" else "Target Days") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddChallenge(newTitle, targetDays.toIntOrNull() ?: 7)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepEmerald)
                ) {
                    Text(if (isArabic) "حفظ التحدي" else "Save", color = AccentGold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SummaryBadge(modifier: Modifier = Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = label, color = SoftWhite.copy(alpha = 0.75f), fontSize = 11.sp)
    }
}

@Composable
private fun OrganizedChallengeCard(
    isArabic: Boolean,
    challenge: DailyChallenge,
    onComplete: () -> Unit
) {
    val progress = (challenge.completedDays.toFloat() / challenge.targetDays.toFloat()).coerceIn(0f, 1f)
    val dayLetters = if (isArabic) listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")
                     else listOf("S", "S", "M", "T", "W", "T", "F")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            1.dp,
            if (challenge.isCompletedToday) AccentGold else Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AccentGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isArabic) challenge.categoryAr else challenge.categoryEn,
                            color = AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) challenge.titleAr else challenge.titleEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "${challenge.completedDays} / ${challenge.targetDays}",
                    color = AccentGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = AccentGold,
                trackColor = DeepEmerald.copy(alpha = 0.35f)
            )

            // شريط أيام الأسبوع السبعة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLetters.forEachIndexed { index, letter ->
                    val done = challenge.weekHistory.getOrElse(index) { false }
                    val circleBg by animateColorAsState(
                        targetValue = if (done) AccentGold else Color.Gray.copy(alpha = 0.15f),
                        label = "dayBg"
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(circleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (done) DeepEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // زر تسجيل إنجاز اليوم المنظم
            Button(
                onClick = onComplete,
                enabled = !challenge.isCompletedToday,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepEmerald,
                    disabledContainerColor = AccentGold.copy(alpha = 0.22f)
                )
            ) {
                Icon(
                    imageVector = if (challenge.isCompletedToday) Icons.Rounded.CheckCircle else Icons.Rounded.TaskAlt,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (challenge.isCompletedToday) {
                        if (isArabic) "تم إنجاز ورد اليوم بنجاح ✓" else "Completed for Today ✓"
                    } else {
                        if (isArabic) "تسجيل إنجاز اليوم" else "Mark Today Complete"
                    },
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
