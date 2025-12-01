package com.example.runnershigh.data.remote.dto

data class RunningFeedbackRequest(
    val sessionId: String,
    val userId: String,
    val rating: Int,
    val difficulty: Int,
    val injuryParts: List<String>,
    val comment: String
)
