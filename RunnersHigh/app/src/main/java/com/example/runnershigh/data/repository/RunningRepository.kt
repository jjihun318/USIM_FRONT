package com.example.runnershigh.data.repository

import com.example.runnershigh.data.remote.RunningApi
import com.example.runnershigh.data.remote.dto.FinishSessionRequest
import com.example.runnershigh.domain.model.RunningStats

/**
 * Running Repository
 * - ViewModel이 Retrofit을 직접 알지 않도록 중간 계층
 */
class RunningRepository(
    private val api: RunningApi
) {

    /**
     * 1) 러닝 세션 시작
     *    POST /sessions/start
     */
    suspend fun startSession(): String {
        val response = api.startSession()
        return response.sessionId
    }

    /**
     * 2) 러닝 세션 종료
     *    PATCH /api/sessions/{sessionId}
     */
    suspend fun finishSession(sessionId: String, stats: RunningStats) {

        // RunningStats → FinishSessionRequest 로 변환
        val requestBody = FinishSessionRequest(
            distance_m = (stats.distanceKm * 1000).toInt(),
            duration_sec = stats.durationSec,
            avg_pace_sec_per_km = stats.paceSecPerKm,
            calories_kcal = stats.calories,
            avg_heart_rate_bpm = stats.avgHeartRate,
            elevation_gain_m = stats.elevationGainM,
            avg_cadence_spm = stats.cadence
        )

        api.finishSession(sessionId, requestBody)
    }

    /**
     * 3) 러닝 결과 조회
     *    GET /sessions/{sessionId}/result
     */
    suspend fun getSessionResult(sessionId: String) =
        api.getSessionResult(sessionId)
}
