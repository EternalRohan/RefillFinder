package com.refillfinder.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity for caching community reports locally.
 * Has a foreign key relationship with CoolerEntity.
 */
@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(
            entity = CoolerEntity::class,
            parentColumns = ["id"],
            childColumns = ["coolerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["coolerId"])]
)
data class ReportEntity(
    @PrimaryKey val id: String,
    val coolerId: String,
    val block: Int,
    val floor: Int,
    val type: String,           // NO_WATER, BROKEN, DIRTY, FIXED, CUSTOM
    val message: String,
    val userName: String,
    val userId: String,
    val upvotes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
