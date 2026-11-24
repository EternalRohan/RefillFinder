package com.refillfinder.app.data.local.dao

import androidx.room.*
import com.refillfinder.app.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Report table.
 * Uses Flow for reactive streams and demonstrates relationships via coolerId FK.
 */
@Dao
interface ReportDao {

    @Query("SELECT * FROM reports WHERE coolerId = :coolerId ORDER BY timestamp DESC")
    fun getReportsForCooler(coolerId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports ORDER BY timestamp DESC LIMIT 100")
    fun getRecentReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE block = :block ORDER BY timestamp DESC")
    fun getReportsForBlock(block: Int): Flow<List<ReportEntity>>

    @Upsert
    suspend fun upsertReport(report: ReportEntity)

    @Upsert
    suspend fun upsertReports(reports: List<ReportEntity>)

    @Query("UPDATE reports SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun incrementUpvotes(id: String)

    @Query("DELETE FROM reports WHERE coolerId = :coolerId")
    suspend fun deleteReportsForCooler(coolerId: String)

    @Query("DELETE FROM reports WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM reports WHERE coolerId = :coolerId")
    suspend fun getReportCountForCooler(coolerId: String): Int
}
