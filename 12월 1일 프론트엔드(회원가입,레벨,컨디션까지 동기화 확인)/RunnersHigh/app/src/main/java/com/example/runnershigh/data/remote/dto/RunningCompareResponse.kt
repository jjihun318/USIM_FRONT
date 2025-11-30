package com.example.runnershigh.data.remote.dto

data class RunningCompareResponse(

    val currentPace: String,         // "6'45\""
    val targetPace: String,          // "6'30\""
    val paceDifferenceSec: Int,      // 15
    val completedDistance: Double,   // 1.8 (Km)
    val remainingDistance: Double,   // 1.2 (Km)
    val estimatedFinishTime: String, // "19:30"
    val currentFinishTime: String,   // "20:15"
    val status: String               // "목표보다 늦음"
)
