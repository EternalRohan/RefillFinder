package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.StationEntity
import com.example.myapplication.data.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State Management for MVVM Architecture
data class MapUiState(
    val isLoading: Boolean = true,
    val stations: List<StationEntity> = emptyList(),
    val selectedStation: StationEntity? = null
)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "stations-db").build()
    private val repository = StationRepository(db.stationDao())

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Seed the local offline database
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDatabase()
        }

        // Single Source of Truth: Observe DB changes using Kotlin Flow
        viewModelScope.launch {
            repository.allStations.collect { stationList ->
                _uiState.update { it.copy(stations = stationList, isLoading = false) }
            }
        }
    }

    // State Hoisting event handler
    fun selectStation(station: StationEntity?) {
        _uiState.update { it.copy(selectedStation = station) }
    }
}
