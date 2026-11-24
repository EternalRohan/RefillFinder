package com.refillfinder.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.refillfinder.app.data.local.entity.HydrationEntity
import com.refillfinder.app.model.Cooler
import com.refillfinder.app.model.CoolerStatus
import com.refillfinder.app.ui.components.BlockFloorSelector
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.utils.CampusHelper
import com.refillfinder.app.utils.Constants
import com.refillfinder.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToCommunity: (String) -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToProfile: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val hydrationStateNullable by viewModel.hydrationState.collectAsStateWithLifecycle()
    val hydration = hydrationStateNullable ?: HydrationEntity(date = viewModel.todayDate, totalMl = 1250, goalMl = 2000, bottlesSaved = 5, streak = 7)

    LaunchedEffect(Unit) { viewModel.seedIfNeeded() }

    val animatedActive by animateIntAsState(
        targetValue = uiState.activeCoolerCount,
        animationSpec = tween(1000), label = "active"
    )

    Scaffold(
        containerColor = BackgroundWhite,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAI,
                containerColor = PrimaryBlue,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🤖", fontSize = 20.sp)
                    Text("WaterBot AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryBlue, AccentTeal))),
                            contentAlignment = Alignment.Center
                        ) { Text("💧", fontSize = 16.sp) }
                        val userName = user?.name?.takeIf { it.isNotBlank() } ?: "Julia"
                        Column {
                            Text("Hello, $userName! 👋", fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp, color = OnSurface)
                            Text("HydroCampus · Central Campus", fontSize = 10.sp, color = OnSurfaceMuted)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleDarkTheme) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isDarkTheme) "☀️" else "🌙", fontSize = 16.sp)
                        }
                    }
                    IconButton(onClick = onNavigateToAI) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 16.sp)
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryBlue, AccentTeal))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (user?.name?.firstOrNull() ?: 'S').toString().uppercase(),
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    scrolledContainerColor = SurfaceWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            var showSearchDialog by remember { mutableStateOf(false) }

            // ── Search bar ──────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable { showSearchDialog = true },
                shape = RoundedCornerShape(14.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderLight),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Search, null, tint = OnSurfaceMuted,
                        modifier = Modifier.size(20.dp))
                    Text("Search building, floor, or station...",
                        color = OnSurfaceSubtle, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Tune, null, tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp))
                }
            }

            if (showSearchDialog) {
                var searchQuery by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showSearchDialog = false },
                    containerColor = SurfaceDark,
                    title = { Text("Search Campus Station", color = OnSurface, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Enter block number (e.g. 15)", color = OnSurfaceSubtle) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Quick suggestions:", color = OnSurfaceMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 15, 24, 40, 60).forEach { b ->
                                    Surface(
                                        onClick = {
                                            viewModel.setLocation(b, 1)
                                            showSearchDialog = false
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = SurfaceVariant,
                                        border = BorderStroke(1.dp, BorderSubtle)
                                    ) {
                                        Text("Block $b", color = OnSurface, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val blockNum = searchQuery.filter { it.isDigit() }.toIntOrNull() ?: uiState.selectedBlock
                                viewModel.setLocation(blockNum.coerceIn(1, 60), uiState.selectedFloor)
                                showSearchDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Go to Block", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSearchDialog = false }) {
                            Text("Cancel", color = OnSurfaceMuted)
                        }
                    }
                )
            }

            var showLocationSelector by remember { mutableStateOf(false) }

            // ── Current Floor & Nearest Cooler Banner ───────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { showLocationSelector = true },
                shape = RoundedCornerShape(14.dp),
                color = PrimaryBlueLight.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📍", fontSize = 20.sp)
                        Column {
                            Text(
                                "Current Location: Block ${uiState.selectedBlock} · Floor ${uiState.selectedFloor}",
                                fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurface
                            )
                            val hasCooler = CampusHelper.hasCoolerOnFloor(uiState.selectedFloor)
                            val nearestFloor = CampusHelper.nearestCoolerFloor(uiState.selectedFloor)
                            Text(
                                if (hasCooler) "✅ Cooler available on this floor!" else "💧 Nearest cooler: Floor $nearestFloor",
                                fontSize = 11.sp, color = PrimaryBlue
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlue
                    ) {
                        Text(
                            "Change", color = Color.White, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (showLocationSelector) {
                BlockFloorSelector(
                    currentBlock = uiState.selectedBlock,
                    currentFloor = uiState.selectedFloor,
                    onDismiss = { showLocationSelector = false },
                    onConfirm = { block, floor ->
                        viewModel.setLocation(block, floor)
                        showLocationSelector = false
                    }
                )
            }

            Spacer(Modifier.height(6.dp))

            // ── Hydration Logger & Goal Ring (Feature 1) ────────────
            HydrationLoggerCard(
                hydration = hydration,
                userName = user?.name ?: "Julia",
                onLogRefill = { viewModel.logRefill(it) },
                onUpdateGoal = { viewModel.updateGoal(it) },
                onUpdateIntake = { viewModel.updateIntake(it) }
            )

            Spacer(Modifier.height(12.dp))

            // ── Nearest refill station card ─────────────────────────
            uiState.nearestCooler?.let { cooler ->
                NearestStationCard(
                    cooler = cooler,
                    block = uiState.selectedBlock,
                    floor = uiState.selectedFloor,
                    onCheckStatus = { onNavigateToCommunity(cooler.id) },
                    onUpdateStatus = { status -> viewModel.updateCoolerStatus(cooler.id, status) }
                )
            }

            Spacer(Modifier.height(16.dp))

            var showServiceDialog by remember { mutableStateOf(false) }

            // ── Stats row ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("💧", "$animatedActive", "Active\nCoolers", GreenActive, GreenLight, Modifier.weight(1f))
                StatCard("🏢", "60", "Campus\nBlocks", PrimaryBlue, PrimaryBlueLight, Modifier.weight(1f))
                StatCard("🔧", "${uiState.serviceCoolers.size}", "Need\nService", YellowReported, YellowLight, Modifier.weight(1f).clickable { showServiceDialog = true })
            }

            if (showServiceDialog) {
                AlertDialog(
                    onDismissRequest = { showServiceDialog = false },
                    containerColor = SurfaceDark,
                    title = { Text("Coolers Needing Service (${uiState.serviceCoolers.size})", color = OnSurface, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier.heightIn(max = 325.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.serviceCoolers.isEmpty()) {
                                Text("🎉 All coolers are active and fully operational!", color = OnSurfaceMuted, fontSize = 13.sp)
                            } else {
                                uiState.serviceCoolers.forEach { cooler ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SurfaceVariant,
                                        border = BorderStroke(1.dp, BorderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Block ${cooler.block} · Floor ${cooler.floor}", fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 13.sp)
                                                Text("Status: ${cooler.status.displayName}", color = when (cooler.status) {
                                                    CoolerStatus.REPORTED -> YellowReported
                                                    CoolerStatus.OFFLINE -> RedOffline
                                                    else -> OnSurfaceMuted
                                                }, fontSize = 11.sp)
                                            }
                                            TextButton(
                                                onClick = {
                                                    showServiceDialog = false
                                                    onNavigateToCommunity(cooler.id)
                                                }
                                            ) {
                                                Text("Inspect", color = PrimaryBlue, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showServiceDialog = false }) {
                            Text("Close", color = OnSurfaceMuted)
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Block grid (Campus Map preview) ─────────────────────
            SectionHeader("Campus Block Overview", "View All Map", onNavigateToMap)
            Spacer(Modifier.height(8.dp))

            // 5-column mini block grid
            val blockRows = (1..60).chunked(10)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                blockRows.take(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        row.forEach { block ->
                            MiniBlockChip(
                                block = block,
                                isSelected = block == uiState.selectedBlock,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setLocation(block, uiState.selectedFloor) }
                            )
                        }
                    }
                }
                TextButton(
                    onClick = onNavigateToMap,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show all 60 blocks →", color = PrimaryBlue, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Nearby coolers horizontal scroll ────────────────────
            SectionHeader("Nearest Refill Stations", "See All", onNavigateToMap)
            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue, strokeWidth = 2.dp)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.nearbyCoolers, key = { it.id }) { cooler ->
                        StationCard(cooler = cooler, onClick = { onNavigateToCommunity(cooler.id) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Recent reports ──────────────────────────────────────
            SectionHeader("Recent Reports", "", {})
            Spacer(Modifier.height(8.dp))

            if (uiState.recentReports.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = GreenLight, shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GreenActive.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎉", fontSize = 24.sp)
                        Column {
                            Text("All coolers operational!", fontWeight = FontWeight.SemiBold,
                                color = GreenActive, fontSize = 14.sp)
                            Text("No issues reported nearby", color = OnSurfaceMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.recentReports.take(4).forEach { report ->
                        ReportItem(
                            report = report,
                            onClick = { onNavigateToCommunity(report.coolerId) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sub-composables ─────────────────────────────────────────────────────────

@Composable
private fun NearestStationCard(
    cooler: Cooler,
    block: Int,
    floor: Int,
    onCheckStatus: () -> Unit,
    onUpdateStatus: (CoolerStatus) -> Unit
) {
    var showUpdateDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        color = SurfaceWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (cooler.status) {
                                CoolerStatus.ACTIVE -> GreenLight
                                CoolerStatus.REPORTED -> YellowLight
                                CoolerStatus.OFFLINE -> RedLight
                                else -> SurfaceVariant
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, when (cooler.status) {
                                CoolerStatus.ACTIVE -> GreenActive
                                CoolerStatus.REPORTED -> YellowReported
                                CoolerStatus.OFFLINE -> RedOffline
                                else -> BorderSubtle
                            })
                        ) {
                            Text(
                                "• ${cooler.status.displayName}",
                                fontSize = 10.sp, color = when (cooler.status) {
                                    CoolerStatus.ACTIVE -> GreenActive
                                    CoolerStatus.REPORTED -> YellowReported
                                    CoolerStatus.OFFLINE -> RedOffline
                                    else -> OnSurface
                                },
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Block $block — ${floor}th Floor",
                        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface
                    )
                    Text(
                        "Water cooler on Floor ${cooler.floor} · Nearest from your location",
                        fontSize = 12.sp, color = OnSurfaceMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryBlueLight),
                    contentAlignment = Alignment.Center
                ) { Text("💧", fontSize = 26.sp) }
            }

            Spacer(Modifier.height(12.dp))

            // ── Live Water Quality & Telemetry Card (Feature 2) ─────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = BackgroundWhite,
                border = BorderStroke(1.dp, BorderLighter)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🧪 Live Water Purity Telemetry", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryBlue)
                        Text("UV-C Active ⚡", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = GreenActive)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("28 PPM", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = OnSurface)
                            Text("TDS Purity", fontSize = 9.sp, color = OnSurfaceMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("7.2", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = OnSurface)
                            Text("pH Level", fontSize = 9.sp, color = OnSurfaceMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("42°F", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = OnSurface)
                            Text("Temp", fontSize = 9.sp, color = OnSurfaceMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("94%", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = GreenActive)
                            Text("Filter Life", fontSize = 9.sp, color = OnSurfaceMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCheckStatus,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryBlue),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                ) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                    Spacer(Modifier.width(6.dp))
                    Text("Check Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryBlue)
                }

                Button(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Update Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Update Cooler Status", color = OnSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select current status:", color = OnSurfaceMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    listOf(CoolerStatus.ACTIVE, CoolerStatus.REPORTED, CoolerStatus.OFFLINE).forEach { status ->
                        Surface(
                            onClick = {
                                onUpdateStatus(status)
                                showUpdateDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = when (status) {
                                CoolerStatus.ACTIVE -> GreenLight
                                CoolerStatus.REPORTED -> YellowLight
                                CoolerStatus.OFFLINE -> RedLight
                                else -> SurfaceVariant
                            },
                            border = BorderStroke(1.dp, when (status) {
                                CoolerStatus.ACTIVE -> GreenActive
                                CoolerStatus.REPORTED -> YellowReported
                                CoolerStatus.OFFLINE -> RedOffline
                                else -> BorderSubtle
                            }),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    when (status) {
                                        CoolerStatus.ACTIVE -> "🟢"
                                        CoolerStatus.REPORTED -> "🟡"
                                        CoolerStatus.OFFLINE -> "🔴"
                                        else -> "⚪"
                                    }, fontSize = 16.sp
                                )
                                Text(status.displayName, color = OnSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        )
    }
}

@Composable
private fun HydrationLoggerCard(
    hydration: HydrationEntity,
    userName: String,
    onLogRefill: (Int) -> Unit,
    onUpdateGoal: (Int) -> Unit,
    onUpdateIntake: (Int) -> Unit
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var showIntakeDialog by remember { mutableStateOf(false) }

    val isGoalAchieved = hydration.totalMl >= hydration.goalMl

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, if (isGoalAchieved) GreenActive.copy(alpha = 0.5f) else BorderLight),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Hydration Goal 💧", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                    Text("Set target & track your daily intake!", fontSize = 11.sp, color = OnSurfaceMuted)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryBlueLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🔥", fontSize = 12.sp)
                        Text("${hydration.streak} Day Streak", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Goal Progress Bar & Manual Edit Buttons
            val progress = (hydration.totalMl.toFloat() / hydration.goalMl.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { showIntakeDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlueLight,
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = PrimaryBlue, modifier = Modifier.size(13.dp))
                        Text("${hydration.totalMl} ml logged", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                    }
                }

                Surface(
                    onClick = { showGoalDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariant,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Goal: ${hydration.goalMl} ml", fontSize = 11.sp, color = OnSurfaceMuted, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.Tune, null, tint = OnSurfaceMuted, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (isGoalAchieved) GreenActive else PrimaryBlue,
                trackColor = PrimaryBlueLight
            )

            // Celebration Banner when Goal Completed!
            if (isGoalAchieved) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GreenLight,
                    border = BorderStroke(1.dp, GreenActive.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🎉", fontSize = 24.sp)
                        Column {
                            Text("Congratulations, $userName! 🏆", fontWeight = FontWeight.Bold, color = GreenActive, fontSize = 13.sp)
                            Text("You achieved your daily goal of ${hydration.goalMl} ml! Stay healthy & hydrated! ✨", color = OnSurface, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Quick refill bottle buttons
            Text("Record Refill Size:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceSubtle)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "🥤 250ml" to 250,
                    "🥛 500ml" to 500,
                    "🍾 750ml" to 750,
                    "🚰 1000ml" to 1000
                ).forEach { (label, ml) ->
                    OutlinedButton(
                        onClick = { onLogRefill(ml) },
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = PrimaryBlueLight.copy(alpha = 0.2f))
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }
    }

    // Set Goal Dialog
    if (showGoalDialog) {
        var customGoal by remember { mutableStateOf(hydration.goalMl.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Set Daily Hydration Goal 🎯", color = OnSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select or enter your target daily water intake:", color = OnSurfaceMuted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2000, 2500, 3000, 3500).forEach { goal ->
                            Surface(
                                onClick = {
                                    onUpdateGoal(goal)
                                    showGoalDialog = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (hydration.goalMl == goal) PrimaryBlue else SurfaceVariant,
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Text("${goal}ml", color = if (hydration.goalMl == goal) Color.White else OnSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = customGoal,
                        onValueChange = { customGoal = it },
                        label = { Text("Custom Goal (ml)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = customGoal.filter { it.isDigit() }.toIntOrNull() ?: hydration.goalMl
                        onUpdateGoal(g)
                        showGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save Goal", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        )
    }

    // Edit Intake Dialog
    if (showIntakeDialog) {
        var customIntake by remember { mutableStateOf(hydration.totalMl.toString()) }
        AlertDialog(
            onDismissRequest = { showIntakeDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Manually Record Intake ✏️", color = OnSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter how much water you have drunk so far today:", color = OnSurfaceMuted, fontSize = 12.sp)
                    OutlinedTextField(
                        value = customIntake,
                        onValueChange = { customIntake = it },
                        label = { Text("Water Drunk So Far (ml)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intake = customIntake.filter { it.isDigit() }.toIntOrNull() ?: hydration.totalMl
                        onUpdateIntake(intake)
                        showIntakeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Update Intake", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIntakeDialog = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    emoji: String, value: String, label: String,
    color: Color, bgColor: Color, modifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(label, color = OnSurfaceMuted, fontSize = 10.sp, lineHeight = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun MiniBlockChip(block: Int, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrimaryBlue else SurfaceWhite,
        border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderLight),
        modifier = modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "B$block",
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else OnSurface
            )
        }
    }
}

@Composable
private fun StationCard(cooler: Cooler, onClick: () -> Unit) {
    val statusColor = when (cooler.status) {
        CoolerStatus.ACTIVE -> GreenActive
        CoolerStatus.REPORTED -> YellowReported
        CoolerStatus.OFFLINE -> RedOffline
        else -> OnSurfaceMuted
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 2.dp,
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlueLight),
                    contentAlignment = Alignment.Center
                ) { Text("💧", fontSize = 20.sp) }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Block ${cooler.block}", fontWeight = FontWeight.Bold,
                fontSize = 14.sp, color = OnSurface)
            Text("Floor ${cooler.floor}", fontSize = 12.sp, color = OnSurfaceMuted)
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (cooler.status) {
                    CoolerStatus.ACTIVE -> GreenLight
                    CoolerStatus.REPORTED -> YellowLight
                    else -> RedLight
                }
            ) {
                Text(
                    cooler.status.displayName,
                    fontSize = 10.sp, color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ReportItem(report: com.refillfinder.app.model.Report, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlueLight),
                contentAlignment = Alignment.Center
            ) { Text(report.reportType.emoji, fontSize = 18.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text("Block ${report.block} · Floor ${report.floor}",
                    fontWeight = FontWeight.SemiBold, color = OnSurface, fontSize = 13.sp)
                Text(
                    report.message.ifBlank { report.reportType.displayName },
                    color = OnSurfaceMuted, fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Text(CampusHelper.timeAgo(report.timestamp.toDate().time),
                color = OnSurfaceSubtle, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
        if (actionLabel.isNotEmpty()) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                Text(actionLabel, color = PrimaryBlue, fontSize = 12.sp)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                    tint = PrimaryBlue, modifier = Modifier.size(14.dp))
            }
        }
    }
}
