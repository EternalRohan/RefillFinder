package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.Building
import com.example.myapplication.data.WaterCoolerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val repository = WaterCoolerRepository()
    private val allBuildings = repository.getBuildings()

    private val _uiState = MutableStateFlow(CampusUiState(buildings = allBuildings))
    val uiState: StateFlow<CampusUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                buildings = filterBuildings(query)
            )
        }
    }

    private fun filterBuildings(query: String): List<Building> {
        if (query.isBlank()) {
            return allBuildings
        }
        return allBuildings.filter { building ->
            building.name.contains(query, ignoreCase = true) || 
            building.id.toString() == query
        }
    }
}

data class CampusUiState(
    val searchQuery: String = "",
    val buildings: List<Building> = emptyList()
)
