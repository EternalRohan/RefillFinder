package com.refillfinder.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.refillfinder.app.data.local.dao.ReportDao
import com.refillfinder.app.data.local.entity.ReportEntity
import com.refillfinder.app.model.Report
import com.refillfinder.app.model.ReportType
import com.refillfinder.app.utils.Constants
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Offline-First ReportRepository.
 * Writes to Room first, then syncs to Firestore asynchronously.
 */
class ReportRepository(
    private val reportDao: ReportDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getReportsForCooler(coolerId: String): Flow<List<Report>> =
        reportDao.getReportsForCooler(coolerId)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }

    fun getRecentReports(): Flow<List<Report>> =
        reportDao.getRecentReports()
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }

    /**
     * Submit a report: write to Room immediately, then push to Firestore.
     * Demonstrates write-through caching + structured concurrency.
     */
    suspend fun submitReport(
        coolerId: String,
        block: Int,
        floor: Int,
        type: ReportType,
        message: String,
        userName: String,
        userId: String
    ): Result<Unit> = runCatching {
        val reportId = UUID.randomUUID().toString()
        val entity = ReportEntity(
            id = reportId,
            coolerId = coolerId,
            block = block,
            floor = floor,
            type = type.name,
            message = message,
            userName = userName,
            userId = userId,
            upvotes = 0,
            timestamp = System.currentTimeMillis()
        )
        // Write to Room immediately (offline-first)
        reportDao.upsertReport(entity)
        reportDao.getReportCountForCooler(coolerId)

        // Sync to Firestore in background
        scope.launch {
            try {
                firestore.collection(Constants.REPORTS_COLLECTION)
                    .document(reportId)
                    .set(entity.toFirestoreMap())
                    .await()
            } catch (_: Exception) { /* Will sync later when online */ }
        }
    }

    suspend fun upvoteReport(reportId: String) {
        reportDao.incrementUpvotes(reportId)
        scope.launch {
            try {
                firestore.collection(Constants.REPORTS_COLLECTION)
                    .document(reportId)
                    .update("upvotes", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
            } catch (_: Exception) {}
        }
    }

    /**
     * Sync Firestore reports for a cooler into Room.
     */
    fun syncReportsForCooler(coolerId: String) {
        scope.launch {
            try {
                val snapshot = firestore.collection(Constants.REPORTS_COLLECTION)
                    .whereEqualTo("coolerId", coolerId)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()

                val entities = snapshot.documents.mapNotNull { doc ->
                    try {
                        ReportEntity(
                            id = doc.id,
                            coolerId = doc.getString("coolerId") ?: coolerId,
                            block = (doc.getLong("block") ?: 1).toInt(),
                            floor = (doc.getLong("floor") ?: 1).toInt(),
                            type = doc.getString("type") ?: ReportType.CUSTOM.name,
                            message = doc.getString("message") ?: "",
                            userName = doc.getString("userName") ?: "Anonymous",
                            userId = doc.getString("userId") ?: "",
                            upvotes = (doc.getLong("upvotes") ?: 0).toInt(),
                            timestamp = (doc.getTimestamp("timestamp")
                                ?.toDate()?.time ?: System.currentTimeMillis())
                        )
                    } catch (_: Exception) { null }
                }
                reportDao.upsertReports(entities)
            } catch (_: Exception) {}
        }
    }

    private fun ReportEntity.toDomain() = Report(
        id = id,
        coolerId = coolerId,
        block = block,
        floor = floor,
        reportType = runCatching { ReportType.valueOf(type) }.getOrDefault(ReportType.CUSTOM),
        message = message,
        userName = userName,
        userId = userId,
        upvotes = upvotes,
        timestamp = Timestamp(java.util.Date(timestamp))
    )

    private fun ReportEntity.toFirestoreMap() = mapOf(
        "coolerId" to coolerId,
        "block" to block,
        "floor" to floor,
        "type" to type,
        "message" to message,
        "userName" to userName,
        "userId" to userId,
        "upvotes" to upvotes,
        "timestamp" to Timestamp(java.util.Date(timestamp))
    )
}
