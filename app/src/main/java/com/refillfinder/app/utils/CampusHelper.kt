package com.refillfinder.app.utils

import com.refillfinder.app.model.Cooler
import com.refillfinder.app.model.CoolerStatus
import java.util.Calendar

object CampusHelper {

    /**
     * Find the cooler for the user's current block and floor, or nearest active cooler.
     */
    fun findNearestCooler(coolers: List<Cooler>, block: Int, floor: Int): Cooler? {
        if (coolers.all { it.status == CoolerStatus.OFFLINE }) return null

        val nearestFloor = nearestCoolerFloor(floor)
        val sameCooler = coolers.find { it.block == block && it.floor == nearestFloor }
        if (sameCooler != null) return sameCooler

        val activeCoolers = coolers.filter { it.status == CoolerStatus.ACTIVE }
        if (activeCoolers.isEmpty()) return null

        return activeCoolers.minByOrNull { cooler ->
            val blockDist = Math.abs(cooler.block - block)
            val floorDist = Math.abs(cooler.floor - floor)
            (blockDist * 10) + floorDist
        }
    }

    /**
     * Returns nearby coolers sorted by proximity (same block first).
     */
    fun getNearbyActiveCoolers(coolers: List<Cooler>, block: Int, limit: Int = 8): List<Cooler> {
        return coolers
            .sortedBy { Math.abs(it.block - block) * 10 + it.floor }
            .take(limit)
    }

    /**
     * Returns which cooler floor is nearest below or at the current floor.
     * Coolers are on floors 1, 3, 5, 7, 9 (odd floors).
     */
    fun nearestCoolerFloor(currentFloor: Int): Int {
        return Constants.COOLER_FLOORS
            .minByOrNull { Math.abs(it - currentFloor) }
            ?: 1
    }

    fun hasCoolerOnFloor(floor: Int): Boolean = floor in Constants.COOLER_FLOORS

    fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..20 -> "Evening"
        else -> "Night"
    }

    fun timeAgo(timestampMs: Long): String {
        val diff = System.currentTimeMillis() - timestampMs
        return when {
            diff < 60_000 -> "just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> "${diff / 86_400_000}d ago"
        }
    }
}
