package com.refillfinder.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.refillfinder.app.ReFillFinderApp
import com.refillfinder.app.model.Report
import com.refillfinder.app.model.ReportType
import com.refillfinder.app.repository.ReportRepository
import com.refillfinder.app.utils.Result as AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * CommunityViewModel
 *
 * Syllabus:
 *  - AndroidViewModel + Room SSOT (offline-first)
 *  - StateFlow — reactive UI state
 *  - viewModelScope structured concurrency
 *  - Flow collection with catch operator
 */
data class CommunityUiState(
    val reports: List<Report> = emptyList(),
    val coolerId: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as ReFillFinderApp).database
    private val repository = ReportRepository(db.reportDao())
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    fun loadReports(coolerId: String) {
        _uiState.value = _uiState.value.copy(coolerId = coolerId, isLoading = true)
        viewModelScope.launch {
            // getReportsForCooler() = correct method name from ReportRepository
            repository.getReportsForCooler(coolerId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = e.message
                    )
                }
                .collect { reports ->
                    _uiState.value = _uiState.value.copy(reports = reports, isLoading = false)
                }
        }
        // Also try to sync from Firestore (runs in background, Room is SSOT)
        repository.syncReportsForCooler(coolerId)
    }

    fun submitReport(
        coolerId: String,
        block: Int,
        floor: Int,
        type: ReportType,
        message: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true, error = null, submitSuccess = false
            )
            // Get current user info for the report
            val currentUser = auth.currentUser
            val userName = currentUser?.displayName ?: "Anonymous"
            val userId   = currentUser?.uid ?: ""

            // submitReport returns kotlin.Result<Unit> (runCatching)
            val result = repository.submitReport(
                coolerId  = coolerId,
                block     = block,
                floor     = floor,
                type      = type,
                message   = message,
                userName  = userName,
                userId    = userId
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false, submitSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to submit"
                )
            }
        }
    }

    fun upvoteReport(reportId: String) {
        viewModelScope.launch { repository.upvoteReport(reportId) }
    }

    fun resetSubmitState() {
        _uiState.value = _uiState.value.copy(submitSuccess = false, error = null)
    }
}
