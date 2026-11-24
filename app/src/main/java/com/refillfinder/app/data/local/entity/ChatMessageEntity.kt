package com.refillfinder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for persisting WaterBot AI chat history across sessions.
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val isFromUser: Boolean,
    val isLoading: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
