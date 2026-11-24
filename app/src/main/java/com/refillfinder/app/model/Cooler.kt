package com.refillfinder.app.model

enum class CoolerStatus(val displayName: String) {
    ACTIVE("Active"),
    REPORTED("Reported"),
    OFFLINE("Offline"),
    UNKNOWN("Unknown")
}

data class Cooler(
    val id: String = "",
    val block: Int = 1,
    val floor: Int = 1,
    val status: CoolerStatus = CoolerStatus.UNKNOWN,
    val reportCount: Int = 0
)
