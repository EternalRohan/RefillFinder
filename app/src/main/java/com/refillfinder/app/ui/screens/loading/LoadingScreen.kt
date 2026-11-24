package com.refillfinder.app.ui.screens.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Post-Login Loading Screen — shown for 2.5 seconds after successful login.
 *
 * Syllabus coverage:
 *  - LaunchedEffect (one-shot coroutine on composition entry)
 *  - AnimatedVisibility, animateFloatAsState
 *  - Coroutine delay (Suspending function)
 *  - InfiniteTransition animations
 */
@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {

    // ── LaunchedEffect: runs once, delays 2.5s, then navigates ──────
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Connecting to campus network...") }

    LaunchedEffect(Unit) {
        // Step 1 — 0.8s
        delay(100)
        val steps = listOf(
            0.3f  to "Connecting to campus network...",
            0.6f  to "Loading cooler database...",
            0.85f to "Syncing real-time status...",
            1.0f  to "All systems ready! 💧"
        )
        for ((target, text) in steps) {
            // Animate progress smoothly
            val current = progress
            val step = (target - current) / 20
            repeat(20) {
                progress += step
                delay(30L)
            }
            progress = target
            statusText = text
            delay(350L)
        }
        delay(400L)
        onLoadingComplete()
    }

    // ── Pulsing water drop ───────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "dropScale"
    )
    val ring by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Animated water drop ──────────────────────────────────
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer pulsing ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(ring)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF1565C0).copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Middle ring
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD))
                )
                // Core drop
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E88E5), Color(0xFF00897B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💧", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                "ReFillFinder",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Text(
                "Campus Water Intelligence",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Progress bar ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(240.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF1565C0),
                    trackColor = Color(0xFFE3F2FD),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    statusText,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
