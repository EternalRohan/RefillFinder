package com.refillfinder.app.data.local.dao

import androidx.room.*
import com.refillfinder.app.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat message persistence.
 * Stores WaterBot conversation history across app sessions.
 */
@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int = 50): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int

    // Keep only last N messages to prevent DB bloat
    @Query("""
        DELETE FROM chat_messages WHERE id NOT IN (
            SELECT id FROM chat_messages ORDER BY timestamp DESC LIMIT :keepCount
        )
    """)
    suspend fun trimToLatest(keepCount: Int = 100)
}
