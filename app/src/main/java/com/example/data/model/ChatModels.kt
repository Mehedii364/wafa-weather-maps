package com.example.data.model

enum class ActionType {
    NONE,
    SHOW_WEATHER,
    SHOW_MAP_PLACE,
    ROUTE_DIRECTIONS
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val actionType: ActionType = ActionType.NONE,
    val actionPayload: String = ""
)

data class PromptSuggestion(
    val id: String,
    val title: String,
    val prompt: String,
    val category: String,
    val icon: String
)
