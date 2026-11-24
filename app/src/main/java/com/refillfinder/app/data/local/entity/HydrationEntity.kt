package com.refillfinder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_logs")
data class HydrationEntity(
    @PrimaryKey
    val date: String,             // Format: YYYY-MM-DD
    val totalMl: Int = 0,
    val goalMl: Int = 2000,
    val bottlesSaved: Int = 0,
    val streak: Int = 1
)
