package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi! I am the RefillFinder AI. I can help you find water stations across all 60 campus buildings. What building are you in?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /
    private val apiKey = "API_KEY"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        systemInstruction = content {
            text(
                "You are RefillFinder, a friendly and helpful AI assistant for a university campus. " +
                "Your goal is to help students find water stations. " +
                "Here is the database of water stations:\n" +
                "${WaterStationData.getContextString()}\n"
            )
        }
    )

    private val chat = generativeModel.startChat()

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        _messages.value = _messages.value + ChatMessage(userMessage, isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                if (apiKey == "API_KEY") {
                    _messages.value = _messages.value + ChatMessage(
                        "⚠️ Please replace 'API_KEY' with your actual Google Gemini API Key in ChatViewModel.kt to use the AI.",
                        isUser = false
                    )
                    return@launch
                }
                
                val response = chat.sendMessage(userMessage)
                _messages.value = _messages.value + ChatMessage(
                    response.text ?: "I couldn't understand that.",
                    isUser = false
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    "Error connecting to AI: ${e.localizedMessage}",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
