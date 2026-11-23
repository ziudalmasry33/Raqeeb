package com.raqeeb.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald
import com.raqeeb.app.ui.theme.SoftWhite
import kotlinx.coroutines.delay

@Composable
fun RescueDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onExitToHome: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DeepEmerald),
            border = BorderStroke(1.5.dp, AccentGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(44.dp)
                )

                Text(
                    text = if (isArabic) "لحظة ثبات.. الله معك" else "A Moment of Strength.. Allah is With You",
                    color = AccentGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isArabic)
                        "﴿ وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ﴾\nسورة الطلاق - آية 2"
                    else
                        "\"And whoever fears Allah - He will make for him a way out.\"\nSurah At-Talaq, Verse 2",
                    color = SoftWhite,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Text(
                    text = if (isArabic)
                        "خذ نفساً عميقاً، استغفر الله، واترك هذه اللحظة تمر بسلام. ستفخر بثباتك بعد دقيقة واحدة."
                    else
                        "Take a deep breath, seek forgiveness, and let this urge pass. You will be proud of your self-control in one minute.",
                    color = SoftWhite.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                    CircularProgressIndicator(
                        progress = { secondsRemaining / 60f },
                        modifier = Modifier.size(84.dp),
                        color = AccentGold,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "${secondsRemaining}s",
                        color = SoftWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onExitToHome,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isArabic) "إغلاق المشتتات والعودة لأهدافي" else "Close Distractions & Return to Goals",
                        color = DeepEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = if (isArabic) "أنا بخير الآن، إغلاق" else "I am calm now, close",
                        color = SoftWhite.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
