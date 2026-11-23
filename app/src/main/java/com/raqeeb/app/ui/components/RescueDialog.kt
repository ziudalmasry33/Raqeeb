package com.raqeeb.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelfImprovement
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

private data class CalmingEntry(
    val verseAr: String,
    val sourceAr: String,
    val verseEn: String,
    val sourceEn: String,
    val guidanceAr: String,
    val guidanceEn: String
)

private val calmingEntries = listOf(
    CalmingEntry(
        verseAr = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
        sourceAr = "سورة الرعد - آية 28",
        verseEn = "\"Unquestionably, by the remembrance of Allah hearts are assured.\"",
        sourceEn = "Surah Ar-Ra'd, Verse 28",
        guidanceAr = "أغلق عينيك لثوانٍ، خذ نفساً عميقاً، وردد: (سبحان الله وبحمده، أستغفر الله العظيم).",
        guidanceEn = "Close your eyes for a moment, breathe deeply, and say: SubhanAllah wa bihamdihi."
    ),
    CalmingEntry(
        verseAr = "﴿ وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ ﴾",
        sourceAr = "سورة الطلاق - آية 2-3",
        verseEn = "\"And whoever fears Allah - He will make for him a way out and provide for him from where he does not expect.\"",
        sourceEn = "Surah At-Talaq, Verses 2-3",
        guidanceAr = "هذه الرغبة عابرة وستزول خلال دقيقة، لكن أجر ثباتك عند الله باقٍ لا يزول.",
        guidanceEn = "This urge is temporary and will pass in a minute, while your reward with Allah remains."
    ),
    CalmingEntry(
        verseAr = "﴿ قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ﴾",
        sourceAr = "سورة الزمر - آية 53",
        verseEn = "\"Say, O My servants who have transgressed against themselves, do not despair of the mercy of Allah.\"",
        sourceEn = "Surah Az-Zumar, Verse 53",
        guidanceAr = "باب التوبة والعودة مفتوح دائماً؛ ابدأ صفحة نقية من هذه اللحظة.",
        guidanceEn = "The door of mercy is always open; start a pure page from this very moment."
    ),
    CalmingEntry(
        verseAr = "«اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ شَرِّ سَمْعِي، وَمِنْ شَرِّ بَصَرِي، وَمِنْ شَرِّ لِسَانِي، وَمِنْ شَرِّ قَلْبِي»",
        sourceAr = "حديث صحيح - سنن أبي داود (1551)",
        verseEn = "\"O Allah, I seek refuge in You from the evil of my hearing, my sight, my tongue, and my heart.\"",
        sourceEn = "Authentic Hadith - Sunan Abi Dawud (1551)",
        guidanceAr = "استعن بالله بصدق، فما استعان عبد بربه إلا أعانه وثبّته.",
        guidanceEn = "Seek Allah's help sincerely; whoever relies on Him will be strengthened."
    ),
    CalmingEntry(
        verseAr = "﴿ وَاصْبِرْ فَإِنَّ اللَّهَ لَا يُضِيعُ أَجْرَ الْمُحْسِنِينَ ﴾",
        sourceAr = "سورة هود - آية 115",
        verseEn = "\"And be patient, for indeed, Allah does not allow to be lost the reward of those who do good.\"",
        sourceEn = "Surah Hud, Verse 115",
        guidanceAr = "كل مجاهدة للنفس ترفع درجتك وتزيد قلبك نوراً وسكينة.",
        guidanceEn = "Every struggle against desire elevates your rank and fills your heart with peace."
    )
)

@Composable
fun RescueDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onExitToHome: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(60) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val current = calmingEntries[currentIndex]

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
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SelfImprovement, contentDescription = null, tint = AccentGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "لحظة سَكينة" else "Moment of Serenity",
                            color = AccentGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = { currentIndex = (currentIndex + 1) % calmingEntries.size }
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "آية أخرى" else "Next",
                            color = AccentGold,
                            fontSize = 12.sp
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftWhite.copy(alpha = 0.07f)),
                    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) current.verseAr else current.verseEn,
                            color = SoftWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 25.sp
                        )
                        Text(
                            text = if (isArabic) current.sourceAr else current.sourceEn,
                            color = AccentGold,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = if (isArabic) current.guidanceAr else current.guidanceEn,
                    color = SoftWhite.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                    CircularProgressIndicator(
                        progress = { secondsRemaining / 60f },
                        modifier = Modifier.size(76.dp),
                        color = AccentGold,
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "${secondsRemaining}s",
                        color = SoftWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onExitToHome,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = DeepEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "العودة لهدوء النفس وإغلاق المشتتات" else "Return to Peace & Close Distractions",
                        color = DeepEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = if (isArabic) "اطمأن قلبي الآن، إغلاق" else "My heart is calm now, close",
                        color = SoftWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
