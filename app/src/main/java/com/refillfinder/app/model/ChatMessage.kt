package com.refillfinder.app.model

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val isFromUser: Boolean = true,
    val isLoading: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
