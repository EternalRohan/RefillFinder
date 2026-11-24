package com.refillfinder.app.data.local.dao

import androidx.room.*
import com.refillfinder.app.data.local.entity.HydrationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {

    @Query("SELECT * FROM hydration_logs WHERE date = :date LIMIT 1")
    fun getLogForDate(date: String): Flow<HydrationEntity?>

    @Query("SELECT * FROM hydration_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDateSync(date: String): HydrationEntity?

    @Upsert
    suspend fun upsertLog(log: HydrationEntity)

    @Query("SELECT SUM(bottlesSaved) FROM hydration_logs")
    fun getTotalBottlesSaved(): Flow<Int?>
}
