package com.refillfinder.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.utils.Constants
import com.refillfinder.app.viewmodel.AuthViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    authViewModel: AuthViewModel
) {
    var selectedBlock by remember { mutableStateOf(1) }
    var selectedFloor by remember { mutableStateOf(1) }
    var step by remember { mutableStateOf(0) } // 0 = block, 1 = floor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(text = "💧", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (step == 0) "Which block are you in?" else "Which floor are you on?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (step == 0)
                    "Select your default block (1–60)"
                else
                    "Select your current floor (1–9)",
                color = OnSurfaceMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .width(if (step == index) 28.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (step == index) PrimaryBlue else BorderSubtle)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (step == 0) {
                // Block selector grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items((1..Constants.TOTAL_BLOCKS).toList()) { block ->
                        val selected = block == selectedBlock
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PrimaryBlue else SurfaceVariant)
                                .border(
                                    1.dp,
                                    if (selected) PrimaryBlueLight else BorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedBlock = block },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$block",
                                color = if (selected) Color.White else OnSurfaceMuted,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                // Floor selector
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items((1..Constants.TOTAL_FLOORS).toList()) { floor ->
                        val selected = floor == selectedFloor
                        val hasCooler = floor in Constants.COOLER_FLOORS
                        Box(
                            modifier = Modifier
                                .aspectRatio(1.6f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) PrimaryBlue else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (selected) PrimaryBlueLight else BorderSubtle,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedFloor = floor },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Floor $floor",
                                    color = if (selected) Color.White else OnSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                if (hasCooler) {
                                    Text(
                                        text = "💧 Cooler",
                                        color = if (selected) Color.White.copy(alpha = 0.8f) else AccentTeal,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (step == 0) {
                        step = 1
                    } else {
                        authViewModel.updateUserLocation(selectedBlock, selectedFloor)
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(
                    text = if (step == 0) "Next →" else "Get Started 🚀",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
