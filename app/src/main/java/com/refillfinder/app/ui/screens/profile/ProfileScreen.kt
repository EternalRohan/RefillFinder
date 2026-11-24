package com.refillfinder.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = OnSurface, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(PrimaryBlue, AccentTeal))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user?.name?.firstOrNull() ?: 'S').toString().uppercase(),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user?.name ?: "Student",
                color = OnSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.email ?: "",
                color = OnSurfaceMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Location info card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📍 Default Location",
                        color = OnSurfaceMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip("Block ${user?.defaultBlock ?: 1}")
                        InfoChip("Floor ${user?.defaultFloor ?: 1}")
                        InfoChip("💧 ${if ((user?.defaultFloor ?: 1) % 2 != 0) "Has Cooler" else "No Cooler"}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("300", "Total\nCoolers")
                    VerticalDivider(color = BorderSubtle, modifier = Modifier.height(40.dp))
                    StatItem("60", "Campus\nBlocks")
                    VerticalDivider(color = BorderSubtle, modifier = Modifier.height(40.dp))
                    StatItem("5", "Cooler\nFloors")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings section
            Text(
                "Settings",
                color = OnSurfaceMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        label = "Dark Theme",
                        trailing = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onToggleDarkTheme() }
                            )
                        },
                        onClick = onToggleDarkTheme
                    )
                    HorizontalDivider(color = BorderSubtle)
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        label = "Notifications",
                        onClick = {}
                    )
                    HorizontalDivider(color = BorderSubtle)
                    SettingsRow(
                        icon = Icons.Default.Info,
                        label = "About ReFillFinder",
                        onClick = {}
                    )
                    HorizontalDivider(color = BorderSubtle)
                    SettingsRow(
                        icon = Icons.Default.BugReport,
                        label = "Send Feedback",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign out
            Button(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedOffline.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, RedOffline.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.Logout, null, tint = RedOffline, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = RedOffline, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Sign Out", color = OnSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out?", color = OnSurfaceMuted) },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.signOut()
                    onSignOut()
                }) {
                    Text("Sign Out", color = RedOffline, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        )
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariant,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Text(
            text = text,
            color = OnSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = PrimaryBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = OnSurfaceMuted, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = OnSurfaceMuted, modifier = Modifier.size(20.dp))
            Text(label, color = OnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceSubtle, modifier = Modifier.size(18.dp))
            }
        }
    }
}
