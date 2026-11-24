package com.refillfinder.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.refillfinder.app.ReFillFinderApp
import com.refillfinder.app.model.Cooler
import com.refillfinder.app.repository.CoolerRepository
import com.refillfinder.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * CampusMapViewModel
 *
 * Syllabus:
 *  - AndroidViewModel (access Application context for Room DB)
 *  - StateFlow for reactive UI state
 *  - viewModelScope + Dispatchers (structured concurrency)
 *  - Room as Single Source of Truth (via CoolerRepository)
 */
data class CampusMapUiState(
    val coolerMap: Map<String, Cooler> = emptyMap(),
    val selectedBlock: Int? = null,
    val isLoading: Boolean = true
)

class CampusMapViewModel(application: Application) : AndroidViewModel(application) {

    // Access Room DB via Application (Offline-First SSOT)
    private val db = (application as ReFillFinderApp).database
    private val repository = CoolerRepository(db.coolerDao())

    private val _uiState = MutableStateFlow(CampusMapUiState())
    val uiState: StateFlow<CampusMapUiState> = _uiState.asStateFlow()

    init {
        observeAllCoolers()
    }

    private fun observeAllCoolers() {
        viewModelScope.launch {
            repository.getAllCoolersFlow()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { coolers ->
                    // Build a map keyed by cooler ID for O(1) lookup
                    val map = coolers.associateBy { it.id }
                    _uiState.value = _uiState.value.copy(
                        coolerMap = map,
                        isLoading = false
                    )
                }
        }
    }

    fun selectBlock(block: Int?) {
        _uiState.value = _uiState.value.copy(selectedBlock = block)
    }

    /**
     * Derive block status from the worst cooler status in that block.
     * Order: OFFLINE > REPORTED > ACTIVE > UNKNOWN
     */
    fun getBlockStatus(block: Int): String {
        val coolerMap = _uiState.value.coolerMap
        val statuses = Constants.COOLER_FLOORS.mapNotNull { floor ->
            coolerMap["block${block}_floor${floor}"]?.status?.name
        }
        return when {
            statuses.isEmpty()              -> "ACTIVE"
            statuses.any { it == "OFFLINE" }   -> "OFFLINE"
            statuses.any { it == "REPORTED" }  -> "REPORTED"
            else                            -> "ACTIVE"
        }
    }
}
