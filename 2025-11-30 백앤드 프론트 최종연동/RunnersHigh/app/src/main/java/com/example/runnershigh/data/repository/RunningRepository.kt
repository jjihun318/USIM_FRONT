package com.example.runnershigh.data.repository

import com.example.runnershigh.data.remote.dto.*
import com.example.runnershigh.domain.model.RunningStats

/**
 * Running Repository
 * - ViewModel이 Retrofit을 직접 알지 않도록 중간 계층
 */
class RunningRepository(
    private val runningApi: RunningApi
) {

    companion object {
        // 🔥 true 이면 서버 안 타고 전부 로컬 더미 데이터로 처리
        private const val USE_FAKE_API = true
    }

    // ----------------------------------
    // 1) 러닝 세션 시작
    //    POST /sessions/start
    // ----------------------------------
    suspend fun startSession(userUuid: String): Result<StartSessionResponse> {

        // 🔥 FAKE 모드: 서버 없이 가짜 세션 시작
        if (USE_FAKE_API) {
            val fake = StartSessionResponse(
                session_uuid = "fake-session-uuid-9999",
                start_time = "2025-01-01T09:00:00Z",
                message = "수고"
            )
            return Result.success(fake)
        }

        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val response = runningApi.startSession(StartSessionRequest(userUuid))
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

        // 🔥 FAKE 모드: 서버 없이도 항상 성공 처리
        if (USE_FAKE_API) {
            val fake = GpsPointResponse(
                message = "Fake GPS point uploaded"
            )
            return Result.success(fake)
        }

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
        sessionUuid: String
    ): Result<RunningCompareResponse> {

        // 🔥 FAKE 모드: 너가 준 DTO 모양에 맞춘 더미 데이터
        if (USE_FAKE_API) {
            val fake = RunningCompareResponse(
                currentPace = "5'45\"",
                targetPace = "5'30\"",
                paceDifferenceSec = 15,
                completedDistance = 3.2,
                remainingDistance = 1.8,
                estimatedFinishTime = "25:00",
                currentFinishTime = "26:00",
                status = "목표보다 약간 느림"
            )
            return Result.success(fake)
        }

        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val resp = runningApi.getRunningComparison(sessionUuid)
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
        stats: RunningStats
    ): Result<FinishSessionResponse> {

        // 🔥 FAKE 모드: 너가 준 FinishSessionResponse 모양에 맞춰서 더미 응답
        if (USE_FAKE_API) {
            val fake = FinishSessionResponse(
                ok = true,
                sessionId = sessionUuid,
                avgHeartRate = 150, // 대충 평균 심박수 값
                kmPace = mapOf(
                    "1km" to 330,  // 5분30초
                    "2km" to 335,  // 5분35초
                    "3km" to 340   // 5분40초
                )
            )
            return Result.success(fake)
        }

        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val body = FinishSessionRequest(
                totalDistanceKm = stats.distanceKm,
                totalTimeSec = stats.durationSec,
                calories = stats.calories,        // 필요 없으면 null 넘어가도 OK
                avgPaceSecPerKm = stats.paceSecPerKm
            )

            val resp = runningApi.finishSession(
                sessionId = sessionUuid,
                body = body
            )

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

        // 🔥 FAKE 모드: 기록 화면에 보여줄 더미 값
        if (USE_FAKE_API) {
            val fake = SessionResultResponse(
                date = "2025-01-01",
                distance = 5.0,
                averagePace = "5'30\"",
                duration = "27:30",
                calories = 320,
                elevationGain = 20,
                cadence = 170,
                completion = 95,
                targetPace = "5'40\"",
                targetFinishTime = "28:00",
                finishTimeComparison = "-30초 (목표보다 빠르게 완주)",
                courseName = "테스트 러닝 코스"
            )
            return Result.success(fake)
        }

        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val resp = runningApi.getSessionResult(sessionUuid)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ====== 내부 포맷팅 헬퍼들 ======

    // paceSecPerKm(초/킬로) -> "6'45\"" 이런 문자열
    private fun formatPaceForApi(secPerKm: Int): String {
        val min = secPerKm / 60
        val sec = secPerKm % 60
        // 예: 6 분 45 초 -> 6'45"
        return String.format("%d'%02d\"", min, sec)
    }

    // durationSec(총 초) -> "20:15" 같은 "분:초" 문자열
    private fun formatDurationForApi(totalSec: Int): String {
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%d:%02d", min, sec)
    }
}
