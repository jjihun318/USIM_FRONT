package com.example.runnershigh.data.remote.dto

/**
 * 러닝 결과 조회 API 응답
 *
 * GET /sessions/{sessionId}/result
 *
 * 예시:
 * {
 *   "date": "2025-01-21",
 *   "distance": 3.0,
 *   "averagePace": "6'45\"",
 *   "duration": "20:15",
 *   "calories": 216,
 *   "elevationGain": 28,
 *   "cadence": 152,
 *   "completion": 97,
 *   "targetPace": "6'30\"",
 *   "targetFinishTime": "19:30",
 *   "finishTimeComparison": "목표보다 늦음",
 *   "courseName": "토요일 오후 러닝"
 * }
 */
data class SessionResultResponse(
    val date: String,
    val distance: Double,
    val averagePace: String,
    val duration: String,
    val calories: Int,
    val elevationGain: Int,
    val cadence: Int,
    val completion: Int,
    val targetPace: String,
    val targetFinishTime: String,
    val finishTimeComparison: String,
    val courseName: String
)
