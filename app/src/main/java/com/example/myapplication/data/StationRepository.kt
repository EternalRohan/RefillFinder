package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class StationRepository(private val dao: StationDao) {
    val allStations: Flow<List<StationEntity>> = dao.getAllStations()

    suspend fun seedDatabase() {
        // Mock Offline-first Data for University Campus
        val mockData = listOf(
            StationEntity(title = "Main Block (Block 32)", description = "Ground floor, near library", latitude = 31.2560, longitude = 75.7051, hasPurifier = true, hasHotWater = false),
            StationEntity(title = "Block 33", description = "Floor 2, near stairs", latitude = 31.2565, longitude = 75.7056, hasPurifier = true, hasHotWater = true),
            StationEntity(title = "Food Court", description = "Near seating area", latitude = 31.2555, longitude = 75.7060, hasPurifier = false, hasHotWater = false),
            StationEntity(title = "Hostel BH-1", description = "Ground floor lobby", latitude = 31.2570, longitude = 75.7040, hasPurifier = true, hasHotWater = true)
        )
        dao.insertStations(mockData)
    }
}
