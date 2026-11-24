package com.refillfinder.app.ui.screens.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.refillfinder.app.model.CoolerStatus
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.utils.Constants
import com.refillfinder.app.viewmodel.CampusMapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCommunity: (String) -> Unit,
    viewModel: CampusMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Campus Map",
                            color = OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "60 blocks • 300 coolers",
                            color = OnSurfaceMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem("🟢", "Active")
                LegendItem("🟡", "Reported")
                LegendItem("🔴", "Offline")
                LegendItem("⚪", "Unknown")
            }

            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading campus data...", color = OnSurfaceMuted)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items((1..Constants.TOTAL_BLOCKS).toList()) { block ->
                        val blockStatus = viewModel.getBlockStatus(block)
                        val borderColor = when (blockStatus) {
                            "ACTIVE" -> GreenActive.copy(alpha = 0.5f)
                            "REPORTED" -> YellowReported.copy(alpha = 0.5f)
                            "OFFLINE" -> RedOffline.copy(alpha = 0.5f)
                            else -> BorderSubtle
                        }
                        val bgColor = when (blockStatus) {
                            "ACTIVE" -> GreenActive.copy(alpha = 0.06f)
                            "REPORTED" -> YellowReported.copy(alpha = 0.06f)
                            "OFFLINE" -> RedOffline.copy(alpha = 0.06f)
                            else -> SurfaceCard
                        }

                        Surface(
                            onClick = {
                                // Navigate to community for first cooler floor in this block
                                onNavigateToCommunity("block${block}_floor1")
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = bgColor,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.aspectRatio(0.85f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when (blockStatus) {
                                        "ACTIVE" -> "🟢"
                                        "REPORTED" -> "🟡"
                                        "OFFLINE" -> "🔴"
                                        else -> "⚪"
                                    },
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "B$block",
                                    color = OnSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                // Mini floor dots
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Constants.COOLER_FLOORS.forEach { floor ->
                                        val cooler = uiState.coolerMap["block${block}_floor${floor}"]
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    when (cooler?.status) {
                                                        CoolerStatus.ACTIVE -> GreenActive
                                                        CoolerStatus.REPORTED -> YellowReported
                                                        CoolerStatus.OFFLINE -> RedOffline
                                                        CoolerStatus.UNKNOWN -> BorderSubtle
                                                        null -> BorderSubtle
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(emoji: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 12.sp)
        Text(label, color = OnSurfaceMuted, fontSize = 11.sp)
    }
}
