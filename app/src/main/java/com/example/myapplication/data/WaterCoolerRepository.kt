package com.example.myapplication.data

class WaterCoolerRepository {
    fun getBuildings(): List<Building> {
       
        return listOf(
            Building(1, "Building 1", listOf("Ground Floor", "2nd Floor")),
            Building(2, "Building 2", listOf("1st Floor", "3rd Floor", "5th Floor")),
            Building(3, "Building 3", listOf("Ground Floor", "4th Floor")),
            Building(4, "Building 4", listOf("2nd Floor")),
            Building(5, "Building 5", listOf("1st Floor", "2nd Floor", "3rd Floor"))
        )
    }
}
