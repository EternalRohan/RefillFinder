package com.refillfinder.app.model

import com.google.firebase.Timestamp

enum class ReportType(val displayName: String, val emoji: String) {
    NO_WATER("No Water", "🚫"),
    BROKEN("Broken / Not Working", "🔧"),
    DIRTY("Unclean / Dirty", "🧹"),
    FIXED("Issue Fixed!", "✅"),
    CUSTOM("Other", "💬")
}

data class Report(
    val id: String = "",
    val coolerId: String = "",
    val block: Int = 1,
    val floor: Int = 1,
    val reportType: ReportType = ReportType.CUSTOM,
    val message: String = "",
    val userName: String = "Anonymous",
    val userId: String = "",
    val upvotes: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)
