package com.raqeeb.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raqeeb.app.data.TrustedAzkarLibrary
import com.raqeeb.app.data.ZikrItem
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald
import com.raqeeb.app.ui.theme.SoftWhite

@Composable
fun AzkarQuranScreen(isArabic: Boolean) {
    val context = LocalContext.current
    var currentQuranMsg by remember { mutableStateOf(TrustedAzkarLibrary.quranMessages.random()) }
    var selectedCategory by remember { mutableStateOf("morning") }
    val remainingCounts = remember {
        mutableStateMapOf<Int, Int>().apply {
            TrustedAzkarLibrary.structuredAzkar.forEach { put(it.id, it.targetCount) }
        }
    }

    val filteredAzkar = remember(selectedCategory) {
        TrustedAzkarLibrary.structuredAzkar.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 86.dp)
    ) {
        // 1. صندوق "رسائل من القرآن" العشوائي التفاعلي
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                border = BorderStroke(1.5.dp, AccentGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(DeepEmerald, Color(0xFF0A3D2E))))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = AccentGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "رسائل من القرآن لك" else "Messages from the Quran",
                                color = AccentGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${currentQuranMsg.verseAr}\n(${currentQuranMsg.surahRef})\n\n${currentQuranMsg.heartMessage}\n— تطبيق رقيب"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة رسالة من القرآن"))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = AccentGold, modifier = Modifier.size(18.dp))
                        }
                    }

                    AnimatedContent(
                        targetState = currentQuranMsg,
                        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(180)) },
                        label = "quranMsgFade"
                    ) { msg ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = msg.verseAr,
                                color = SoftWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = msg.surahRef,
                                color = AccentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Black.copy(alpha = 0.28f)
                            ) {
                                Text(
                                    text = msg.heartMessage,
                                    color = SoftWhite.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val others = TrustedAzkarLibrary.quranMessages.filter { it != currentQuranMsg }
                            currentQuranMsg = others.random()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = DeepEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "افتح رسالة قرآنية جديدة" else "Open New Quranic Message",
                            color = DeepEmerald,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 2. تبويبات تقسيم الأذكار (الصباح | المساء | أدعية الثبات)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    "morning" to (if (isArabic) "☀️ أذكار الصباح" else "Morning"),
                    "evening" to (if (isArabic) "🌙 أذكار المساء" else "Evening"),
                    "dua" to (if (isArabic) "🤲 أدعية الثبات" else "Duas")
                )
                tabs.forEach { (key, label) ->
                    val selected = selectedCategory == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) DeepEmerald else Color.Transparent)
                            .clickable { selectedCategory = key }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. قائمة الأذكار المنظمة مع عدّاد تسبيح تفاعلي بالضغط
        items(filteredAzkar, key = { it.id }) { item ->
            val left = remainingCounts[item.id] ?: item.targetCount
            ZikrCounterCard(
                item = item,
                remaining = left,
                onTap = {
                    if (left > 0) remainingCounts[item.id] = left - 1
                },
                onReset = {
                    remainingCounts[item.id] = item.targetCount
                }
            )
        }
    }
}

@Composable
private fun ZikrCounterCard(
    item: ZikrItem,
    remaining: Int,
    onTap: () -> Unit,
    onReset: () -> Unit
) {
    val isDone = remaining == 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) DeepEmerald.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (isDone) AccentGold else Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = item.textAr,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 25.sp,
                color = if (isDone) SoftWhite.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.reference,
                    fontSize = 11.sp,
                    color = AccentGold
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isDone) {
                        IconButton(onClick = onReset, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset", tint = AccentGold, modifier = Modifier.size(16.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isDone) AccentGold else DeepEmerald)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDone) "تم ✓" else "المتبقي: $remaining",
                            color = if (isDone) DeepEmerald else AccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
