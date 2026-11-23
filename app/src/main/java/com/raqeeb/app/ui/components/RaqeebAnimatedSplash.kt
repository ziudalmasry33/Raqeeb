package com.raqeeb.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald
import com.raqeeb.app.ui.theme.SoftWhite
import kotlinx.coroutines.delay

@Composable
fun RaqeebAnimatedSplash(
    isArabic: Boolean,
    onSplashFinished: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }

    val shieldScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.45f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splashScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "splashAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(1450L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF041C15), DeepEmerald, Color(0xFF06251C))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.alpha(contentAlpha)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .border(2.dp, AccentGold.copy(alpha = pulseAlpha), CircleShape)
                )

                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .scale(shieldScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(AccentGold.copy(alpha = 0.28f), Color.Transparent)
                            )
                        )
                        .border(2.dp, AccentGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Text(
                text = if (isArabic) "رَقِيب" else "RAQEEB",
                color = AccentGold,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = if (isArabic) "حصنٌ لبصرك.. وسكينةٌ لقلبك" else "Sanctuary for your gaze & heart",
                color = SoftWhite.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
