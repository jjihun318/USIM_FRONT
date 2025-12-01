package com.example.runnershigh.data.remote.dto

data class HeartRateData(
    val bpm: Double,
    val time: String
)

data class HealthData(
    val heartRates: List<HeartRateData>,
    val steps: Int,
    val calories: Double,
    val measuredAt: String // yyyy-MM-dd HH:mm:ss
)
