package com.example.runnershigh.data.repository

import com.example.runnershigh.data.remote.dto.*
import com.example.runnershigh.domain.model.*


/**
 * Running Repository
 * - ViewModel이 Retrofit을 직접 알지 않도록 중간 계층
 */
class RunningRepository(
    private val runningApi: RunningApi
) {



    // ----------------------------------
    // 1) 러닝 세션 시작
    //    POST /sessions/start
    // ----------------------------------
    suspend fun startSession(userId: String): Result<StartSessionResponse> {



        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val response = runningApi.startSession(StartSessionRequest(userId))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ----------------------------------
    // 2) GPS 포인트 업로드
    //    POST /sessions/{sessionId}/gps
    // ----------------------------------
    suspend fun uploadGpsPoint(
        sessionUuid: String,
        latitude: Double,
        longitude: Double,
        timestamp: String
    ): Result<GpsPointResponse> {



        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val body = GpsPointRequest(
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp
            )

            val response = runningApi.uploadGpsPoint(sessionUuid, body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------
    // 3) 러닝 비교 (목표 vs 현재)
    //    GET /sessions/{sessionId}/compare
    // ----------------------------------
    suspend fun getRunningComparison(
        sessionUuid: String,
        distanceMeters: Double,
        elapsedSeconds: Int
    ): Result<RunningCompareResponse> {



        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val resp = runningApi.getRunningComparison(sessionUuid, distanceMeters, elapsedSeconds)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------
    // 4) 러닝 종료
    //    PATCH /sessions/{sessionId}/finish
    // ----------------------------------
    suspend fun finishSession(
        sessionUuid: String,
        userUuid: String,
        stats: RunningStats,
        goal: RunningPlanGoal?,
        goalCompleted: Boolean
    ): Result<FinishSessionResponse> {



        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val body = FinishSessionRequest(
                sessionId = sessionUuid,
                userId = userUuid,
                totalDistanceKm = stats.distanceKm,
                totalTimeSec = stats.durationSec,
                calories = stats.calories,        // 필요 없으면 null 넘어가도 OK
                avgPaceSecPerKm = stats.paceSecPerKm,
                avgHeartRate = stats.avgHeartRate,
                elevationGainM = stats.elevationGainM,
                cadence = stats.cadence,
                targetDistanceKm = goal?.targetDistanceKm,
                targetPaceSecPerKm = goal?.targetPaceSecPerKm,
                goalCompleted = goalCompleted
            )

            val resp = runningApi.finishSession(body)

            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------
    // 5) 러닝 결과 조회 (기록 화면)
    //    GET /sessions/{sessionId}/result
    // ----------------------------------
    suspend fun getRunningResult(
        sessionUuid: String
    ): Result<SessionResultResponse> {



        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val resp = runningApi.getSessionResult(sessionUuid)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ====== 내부 포맷팅 헬퍼들 ======

    suspend fun submitFeedback(
        request: RunningFeedbackRequest
    ): Result<RunningFeedbackResponse> {
        return try {
            val resp = runningApi.submitFeedback(request)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
}
