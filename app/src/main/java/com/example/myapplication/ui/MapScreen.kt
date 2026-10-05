package com.example.myapplication.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.StationEntity
import com.example.myapplication.viewmodel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Campus Default Location
    val campusLocation = LatLng(31.2560, 75.7051)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(campusLocation, 16f)
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    
    // Runtime Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasLocationPermission = isGranted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val mapProperties = MapProperties(isMyLocationEnabled = hasLocationPermission)
    val mapUiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(campusLocation, 16f)
            }) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Google Maps Compose Integration
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings
            ) {
                // Markers populated from UI State (Offline-first data)
                uiState.stations.forEach { station ->
                    Marker(
                        state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                        title = station.title,
                        snippet = "Tap for details",
                        onClick = {
                            viewModel.selectStation(station)
                            true // Consume click to show custom bottom sheet instead of info window
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Material 3 Bottom Sheet
    if (uiState.selectedStation != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectStation(null) },
            sheetState = rememberModalBottomSheetState()
        ) {
            StationDetailSheet(uiState.selectedStation!!)
        }
    }
}

// Custom Composable Design Pattern
@Composable
fun StationDetailSheet(station: StationEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalDrink, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = station.title, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Location: ${station.description}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row {
            FilterChip(selected = station.hasPurifier, onClick = {}, label = { Text("Purifier") })
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(selected = station.hasHotWater, onClick = {}, label = { Text("Hot Water") })
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}
