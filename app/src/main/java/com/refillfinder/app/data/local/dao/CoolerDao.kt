package com.refillfinder.app.data.local.dao

import androidx.room.*
import com.refillfinder.app.data.local.entity.CoolerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Cooler table.
 * Demonstrates: Room DAOs, Flow queries, Upsert, conditional updates.
 */
@Dao
interface CoolerDao {

    // Reactive query — emits new list whenever data changes (Room + Flow)
    @Query("SELECT * FROM coolers ORDER BY block ASC, floor ASC")
    fun getAllCoolers(): Flow<List<CoolerEntity>>

    @Query("SELECT * FROM coolers WHERE block = :block ORDER BY floor ASC")
    fun getCoolersForBlock(block: Int): Flow<List<CoolerEntity>>

    @Query("SELECT * FROM coolers WHERE id = :id")
    suspend fun getCoolerById(id: String): CoolerEntity?

    @Query("SELECT * FROM coolers WHERE status = 'ACTIVE' ORDER BY block ASC, floor ASC")
    fun getActiveCoolers(): Flow<List<CoolerEntity>>

    // Upsert = INSERT OR REPLACE — handles both insert and update
    @Upsert
    suspend fun upsertCooler(cooler: CoolerEntity)

    @Upsert
    suspend fun upsertCoolers(coolers: List<CoolerEntity>)

    @Query("UPDATE coolers SET status = :status, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE coolers SET reportCount = reportCount + 1 WHERE id = :id")
    suspend fun incrementReportCount(id: String)

    @Query("SELECT * FROM coolers")
    suspend fun getAllCoolersList(): List<CoolerEntity>

    @Query("SELECT COUNT(*) FROM coolers")
    suspend fun getCoolerCount(): Int

    @Query("DELETE FROM coolers")
    suspend fun clearAll()
}
