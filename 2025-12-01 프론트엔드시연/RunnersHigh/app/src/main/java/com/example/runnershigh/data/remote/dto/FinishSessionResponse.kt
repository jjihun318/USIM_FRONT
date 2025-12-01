package com.example.runnershigh.data.remote.dto

data class FinishSessionResponse(
    val ok: Boolean,
    val sessionId: String,
    val avgHeartRate: Int?,
    val kmPace: Map<String, Int>
)
