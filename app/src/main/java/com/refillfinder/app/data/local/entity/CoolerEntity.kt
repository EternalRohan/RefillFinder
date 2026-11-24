package com.refillfinder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for caching cooler data locally.
 * Enables offline-first access to cooler status without network.
 */
@Entity(tableName = "coolers")
data class CoolerEntity(
    @PrimaryKey val id: String,           // e.g. "block1_floor1"
    val block: Int,
    val floor: Int,
    val status: String = "UNKNOWN",       // ACTIVE, REPORTED, OFFLINE, UNKNOWN
    val reportCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
