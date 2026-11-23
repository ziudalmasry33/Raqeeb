package com.raqeeb.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raqeeb.app.domain.model.DailyChallenge
import com.raqeeb.app.ui.theme.AccentGold

@Composable
fun ChallengesScreen(
    isArabic: Boolean,
    challenges: List<DailyChallenge>,
    onToggleChallenge: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = AccentGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isArabic) "التحديات اليومية والارتقاء" else "Daily Challenges & Growth",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(challenges, key = { it.id }) { challenge ->
                val progress = (challenge.completedDays.toFloat() / challenge.targetDays).coerceIn(0f, 1f)
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
                                text = if (isArabic) challenge.titleAr else challenge.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onToggleChallenge(challenge.id) }) {
                                Icon(
                                    imageVector = if (challenge.isCompletedToday)
                                        Icons.Rounded.CheckCircle
                                    else
                                        Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Complete Today",
                                    tint = AccentGold
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AccentGold
                        )

                        Text(
                            text = if (isArabic)
                                "الإنجاز: ${challenge.completedDays} من ${challenge.targetDays} يوم"
                            else
                                "Progress: ${challenge.completedDays} / ${challenge.targetDays} days",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
