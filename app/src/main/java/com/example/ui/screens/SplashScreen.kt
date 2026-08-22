package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    currentLanguage: AppLanguage,
    onSplashFinished: () -> Unit
) {
    var progressPercent by remember { mutableFloatStateOf(20f) }
    var statusText by remember { mutableStateOf(AppStrings.searchUpdates(currentLanguage)) }

    val infiniteTransition = rememberInfiniteTransition(label = "ring_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(600)
        progressPercent = 45f
        delay(600)
        progressPercent = 80f
        statusText = AppStrings.connectingServer(currentLanguage)
        delay(800)
        progressPercent = 100f
        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101221)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Logo Ring Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Background Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF1E2238),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Rotating Accent Glow Ring
                Canvas(modifier = Modifier.fillMaxSize().rotate(rotationAngle)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF6C5CE7).copy(alpha = 0.1f),
                                Color(0xFF00D2FF),
                                Color(0xFF6C5CE7)
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner "1R" Rocket Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFF1F2440), Color(0xFF15182C))
                            ),
                            shape = CircleShape
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "1R",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Percentage Text
            Text(
                text = "${progressPercent.toInt()}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C5CE7)
            )

            Spacer(modifier = Modifier.height(100.dp))

            // Server Connection Status
            Text(
                text = statusText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFC0C7D6),
                textAlign = TextAlign.Center
            )
        }
    }
}
