package com.raqeeb.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AzkarQuranScreen(isArabic: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentQuranMsg by remember { mutableStateOf(TrustedAzkarLibrary.quranMessages.random()) }
    var isInspiring by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("morning") }
    val remainingCounts = remember {
        mutableStateMapOf<Int, Int>().apply {
            TrustedAzkarLibrary.structuredAzkar.forEach { put(it.id, it.targetCount) }
        }
    }

    val spinAngle by animateFloatAsState(
        targetValue = if (isInspiring) 360f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "inspireSpin"
    )
    val cardPulse by animateFloatAsState(
        targetValue = if (isInspiring) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardPulse"
    )

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
        item {
            Card(
                modifier = Modifier.scale(cardPulse),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                border = BorderStroke(1.5.dp, AccentGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFF041C15), DeepEmerald, Color(0xFF0A3D2E))))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentGold.copy(alpha = 0.16f))
                                    .border(1.dp, AccentGold.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(spinAngle)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "رسالة من القرآن لك" else "Message from the Quran",
                                    color = AccentGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = if (isArabic) "نورٌ يضيء دربك في كل لحظة" else "Light for your heart",
                                    color = SoftWhite.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
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
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة رسالة من القرآن لك"))
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = AccentGold, modifier = Modifier.size(18.dp))
                        }
                    }

                    AnimatedContent(
                        targetState = currentQuranMsg,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.88f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(tween(300))) togetherWith
                                (scaleOut(targetScale = 0.92f, animationSpec = tween(200)) + fadeOut(tween(200)))
                        },
                        label = "quranInsightAnim"
                    ) { msg ->
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = msg.verseAr,
                                color = SoftWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp,
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
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Black.copy(alpha = 0.30f),
                                border = BorderStroke(0.5.dp, AccentGold.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = msg.heartMessage,
                                    color = SoftWhite.copy(alpha = 0.92f),
                                    fontSize = 13.sp,
                                    lineHeight = 21.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (!isInspiring) {
                                scope.launch {
                                    isInspiring = true
                                    delay(220L)
                                    val others = TrustedAzkarLibrary.quranMessages.filter { it != currentQuranMsg }
                                    currentQuranMsg = others.random()
                                    delay(330L)
                                    isInspiring = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = DeepEmerald,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(spinAngle)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "رسائل من القرآن" else "Messages from Quran",
                            color = DeepEmerald,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

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
