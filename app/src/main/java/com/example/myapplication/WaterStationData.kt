package com.example.myapplication

object WaterStationData {
    val stations: List<WaterStation> = (1..60).flatMap { building ->
        val numStations = when {
            building % 5 == 0 -> 3
            building % 2 == 0 -> 2
            else -> 1
        }
        
        (1..numStations).map { floor ->
            WaterStation(
                buildingNumber = building,
                floor = floor,
                locationDescription = "Near the central staircase on Floor $floor, Building $building",
                hasHotWater = (floor % 2 == 0)
            )
        }
    }

    fun getContextString(): String {
        return "The campus has 60 buildings. Here is the database of water stations:\n" +
            stations.joinToString("\n") { 
                "- Building ${it.buildingNumber}, Floor ${it.floor}: ${it.locationDescription}. Cold Water: ${if(it.hasColdWater) "Yes" else "No"}, Hot Water: ${if(it.hasHotWater) "Yes" else "No"}, Purifier: ${if(it.hasPurifier) "Yes" else "No"}."
            } + "\n\nPlease use this data to accurately answer user questions. Be friendly and helpful!"
    }
}
