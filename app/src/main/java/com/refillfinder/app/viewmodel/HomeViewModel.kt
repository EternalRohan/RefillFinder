package com.refillfinder.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.refillfinder.app.ReFillFinderApp
import com.refillfinder.app.data.local.entity.HydrationEntity
import com.refillfinder.app.model.Cooler
import com.refillfinder.app.model.CoolerStatus
import com.refillfinder.app.model.Report
import com.refillfinder.app.model.User
import com.refillfinder.app.repository.CoolerRepository
import com.refillfinder.app.repository.ReportRepository
import com.refillfinder.app.utils.CampusHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "HomeViewModel"

// ── UI State ─────────────────────────────────────────────────────────────────
data class HomeUiState(
    val isLoading: Boolean = true,
    val selectedBlock: Int = 1,
    val selectedFloor: Int = 1,
    val nearestCooler: Cooler? = null,
    val nearbyCoolers: List<Cooler> = emptyList(),
    val serviceCoolers: List<Cooler> = emptyList(),
    val recentReports: List<Report> = emptyList(),
    val activeCoolerCount: Int = 0,
    val syncError: String? = null
)

// ── One-time Events (SharedFlow) ─────────────────────────────────────────────
sealed class HomeEvent {
    data class ShowSnackbar(val message: String) : HomeEvent()
    object SyncSuccess : HomeEvent()
    data class SyncError(val message: String) : HomeEvent()
}

class HomeViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val db       = (application as ReFillFinderApp).database
    private val coolerRepo = CoolerRepository(db.coolerDao())
    private val reportRepo = ReportRepository(db.reportDao())
    private val auth     = FirebaseAuth.getInstance()

    // ── StateFlow: main UI state ─────────────────────────────────────
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ── SharedFlow: one-time events (collect once, not replayed) ─────
    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    // ── SavedStateHandle: survives process death ──────────────────────
    private val selectedBlock = savedStateHandle.getStateFlow("block", 1)
    private val selectedFloor = savedStateHandle.getStateFlow("floor", 1)

    // ── Current user ─────────────────────────────────────────────────
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ── Hydration & Impact Tracking ─────────────────────────────────
    val todayDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    private val hydrationDao = db.hydrationDao()

    val hydrationState: StateFlow<HydrationEntity> = hydrationDao.getLogForDate(todayDate)
        .map { it ?: HydrationEntity(date = todayDate, totalMl = 1250, goalMl = 2000, bottlesSaved = 5, streak = 7) }
        .catch { emit(HydrationEntity(date = todayDate, totalMl = 1250, goalMl = 2000, bottlesSaved = 5, streak = 7)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HydrationEntity(date = todayDate, totalMl = 1250, goalMl = 2000, bottlesSaved = 5, streak = 7)
        )

    fun logRefill(amountMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = hydrationDao.getLogForDateSync(todayDate) ?: HydrationEntity(date = todayDate, totalMl = 1250, goalMl = 2000, bottlesSaved = 5, streak = 7)
            val newTotal = current.totalMl + amountMl
            val newBottles = current.bottlesSaved + (amountMl / 250).coerceAtLeast(1)
            val updated = current.copy(
                totalMl = newTotal,
                bottlesSaved = newBottles
            )
            hydrationDao.upsertLog(updated)
        }
    }

    fun updateGoal(newGoalMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = hydrationDao.getLogForDateSync(todayDate) ?: HydrationEntity(date = todayDate, totalMl = 1250, goalMl = 2000, bottlesSaved = 5, streak = 7)
            val updated = current.copy(goalMl = newGoalMl.coerceAtLeast(500))
            hydrationDao.upsertLog(updated)
        }
    }

    fun updateIntake(newTotalMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = hydrationDao.getLogForDateSync(todayDate) ?: HydrationEntity(date = todayDate, totalMl = 1250, goalMl = 2000, bottlesSaved = 5, streak = 7)
            val updated = current.copy(totalMl = newTotalMl.coerceAtLeast(0))
            hydrationDao.upsertLog(updated)
        }
    }

    init {
        observeData()
        startFirestoreSync()
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            db.userDao().getStoredUser().collect { userEntity ->
                if (userEntity != null) {
                    _currentUser.value = userEntity.toDomain()
                } else if (auth.currentUser != null) {
                    _currentUser.value = User(
                        uid = auth.currentUser!!.uid,
                        name = auth.currentUser!!.displayName ?: "Julia",
                        email = auth.currentUser!!.email ?: "julia@campus.edu"
                    )
                } else {
                    _currentUser.value = User(uid = "guest_user", name = "Julia", email = "julia@campus.edu")
                }
            }
        }
    }

    /**
     * combine() — Flow operator: merges 4 flows into a single emission.
     * Uses Dispatchers.IO for DB, then maps back to UI thread automatically.
     */
    private fun observeData() {
        viewModelScope.launch {
            combine(
                coolerRepo.getAllCoolersFlow(),
                reportRepo.getRecentReports(),
                selectedBlock,
                selectedFloor
            ) { coolers, reports, block, floor ->
                // derivedStateOf equivalent — only compute when inputs change
                val nearest = CampusHelper.findNearestCooler(coolers, block, floor)
                val nearby  = CampusHelper.getNearbyActiveCoolers(coolers, block)
                val serviceList = coolers.filter { it.status == CoolerStatus.REPORTED || it.status == CoolerStatus.OFFLINE }
                HomeUiState(
                    isLoading        = false,
                    selectedBlock    = block,
                    selectedFloor    = floor,
                    nearestCooler    = nearest,
                    nearbyCoolers    = nearby,
                    serviceCoolers   = serviceList,
                    recentReports    = reports,
                    activeCoolerCount = coolers.count { it.status == CoolerStatus.ACTIVE }
                )
            }
                .catch { e ->
                    // Exception handling in Flow
                    Log.e(TAG, "Data observation failed", e)
                    _uiState.update { it.copy(isLoading = false, syncError = e.message) }
                    _events.emit(HomeEvent.SyncError(e.message ?: "Unknown error"))
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * withTimeout — cancels Firestore sync if it takes > 8 seconds.
     * Demonstrates Coroutine Cancellation and Timeouts.
     */
    private fun startFirestoreSync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withTimeout(8_000L) {
                    coolerRepo.startFirestoreSync()
                        .catch { e -> Log.w(TAG, "Sync failed — using cached data", e) }
                        .collect { }
                }
            } catch (e: CancellationException) {
                // Expected when timeout fires — Room cache is still valid
                Log.d(TAG, "Firestore sync timed out — offline mode active")
                _events.emit(HomeEvent.ShowSnackbar("Offline mode — showing cached data"))
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected sync error", e)
            }
        }
    }

    fun seedIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { coolerRepo.seedCoolersIfEmpty() }
                .onFailure { Log.e(TAG, "Seeding failed", it) }
        }
    }

    fun setLocation(block: Int, floor: Int) {
        // SavedStateHandle persists across process death
        savedStateHandle["block"] = block
        savedStateHandle["floor"] = floor
    }

    fun updateCoolerStatus(coolerId: String, status: CoolerStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            coolerRepo.updateCoolerStatus(coolerId, status)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(syncError = null) }
    }
}
