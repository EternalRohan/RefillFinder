package com.refillfinder.app.ui.screens.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.refillfinder.app.model.Report
import com.refillfinder.app.model.ReportType
import com.refillfinder.app.ui.components.ReportBottomSheet
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.utils.CampusHelper
import com.refillfinder.app.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    coolerId: String,
    onNavigateBack: () -> Unit,
    viewModel: CommunityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showReportSheet by remember { mutableStateOf(false) }

    // Parse block and floor from coolerId (format: block{n}_floor{n})
    val parts = coolerId.removePrefix("block").split("_floor")
    val block = parts.getOrNull(0)?.toIntOrNull() ?: 1
    val floor = parts.getOrNull(1)?.toIntOrNull() ?: 1

    LaunchedEffect(coolerId) {
        viewModel.loadReports(coolerId)
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Community Board",
                            color = OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Block $block • Floor $floor",
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
                actions = {
                    IconButton(onClick = { showReportSheet = true }) {
                        Icon(Icons.Default.Add, null, tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showReportSheet = true },
                containerColor = PrimaryBlue,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            } else if (uiState.reports.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No reports yet",
                                color = OnSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Be the first to report this cooler's status!",
                                color = OnSurfaceMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        onUpvote = { viewModel.upvoteReport(report.id) }
                    )
                }
            }
        }
    }

    if (showReportSheet) {
        val tempCooler = com.refillfinder.app.model.Cooler(
            id = coolerId,
            block = block,
            floor = floor
        )
        ReportBottomSheet(
            cooler = tempCooler,
            isSubmitting = uiState.isSubmitting,
            onDismiss = {
                showReportSheet = false
                viewModel.resetSubmitState()
            },
            onSubmit = { type, message ->
                viewModel.submitReport(coolerId, block, floor, type, message)
            }
        )
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            showReportSheet = false
            viewModel.resetSubmitState()
        }
    }
}

@Composable
private fun ReportCard(report: Report, onUpvote: () -> Unit) {
    val reportType = report.reportType
    val typeColor = when (reportType) {
        ReportType.NO_WATER, ReportType.BROKEN -> RedOffline
        ReportType.DIRTY -> YellowReported
        ReportType.FIXED -> GreenActive
        ReportType.CUSTOM -> AccentTeal
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (report.userName.firstOrNull() ?: 'A').toString().uppercase(),
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(
                            text = report.userName,
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = CampusHelper.timeAgo(
                                report.timestamp.toDate().time
                            ),
                            color = OnSurfaceMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Report type badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, typeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${reportType.emoji} ${reportType.displayName}",
                        color = typeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (report.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = report.message,
                    color = OnSurface,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onUpvote,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        null,
                        tint = if (report.upvotes > 0) PrimaryBlue else OnSurfaceMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${report.upvotes} helpful",
                    color = OnSurfaceMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
