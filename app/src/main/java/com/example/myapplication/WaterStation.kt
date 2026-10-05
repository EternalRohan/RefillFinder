package com.example.myapplication

data class WaterStation(
    val buildingNumber: Int,
    val floor: Int,
    val locationDescription: String,
    val hasColdWater: Boolean = true,
    val hasHotWater: Boolean = false,
    val hasPurifier: Boolean = true
)
