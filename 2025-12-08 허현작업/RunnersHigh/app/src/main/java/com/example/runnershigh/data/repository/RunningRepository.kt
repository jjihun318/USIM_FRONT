package com.example.runnershigh.data.repository

import android.util.Base64
import com.example.runnershigh.data.remote.ApiClient
import com.example.runnershigh.data.remote.dto.*
import com.example.runnershigh.domain.model.*
import com.naver.maps.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/**
 * Running Repository
 * - ViewModel이 Retrofit을 직접 알지 않도록 중간 계층
 */
class RunningRepository(
    private val runningApi: RunningApi
) {

    private var lastNewBadges: List<AcquiredBadge> = emptyList()
    private var lastGainedExperience: Int = 0



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

            lastNewBadges = resp.newBadges
            lastGainedExperience = resp.levelInfo?.gained_xp ?: 0

            Result.success(resp)
        } catch (e: Exception) {
            lastNewBadges = emptyList()
            lastGainedExperience = 0
            Result.failure(e)
        }
    }

    // ----------------------------------
    // 5) 러닝 결과 조회 (기록 화면)
    //    GET /sessions/{sessionId}/result
    // ----------------------------------
    suspend fun getRunningResult(
        sessionUuid: String,
        userUuid: String,
    ): Result<SessionResultResponse> {



        // 🔽 실제 서버 연동 코드 (기존 로직)
        return try {
            val resp = runningApi.getSessionResult(sessionUuid, userUuid)
            val badgeAcquired = lastNewBadges.isNotEmpty() || resp.badgeAcquired
            val gainedExperience = resolveGainedExperience(resp)

            val updatedResponse = resp.copy(
                badgeAcquired = badgeAcquired,
                gainedExperience = gainedExperience
            )

            lastNewBadges = emptyList()
            lastGainedExperience = 0

            Result.success(updatedResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateUserLevel(userUuid: String) {
        runCatching {
            ApiClient.authApi.updateUserLevel(
                UserIdRequest(userId = userUuid, user_uuid = userUuid)
            )
        }
    }

    private fun calculateGainedExperience(current: Int, newBadgeCount: Int): Int {
        return if (current > 0) current else newBadgeCount
    }

    private fun resolveGainedExperience(response: SessionResultResponse): Int {
        val experienceFromLevelUp = lastGainedExperience.takeIf { it > 0 }
        if (experienceFromLevelUp != null) return experienceFromLevelUp

        if (lastNewBadges.isNotEmpty()) {
            return calculateGainedExperience(response.gainedExperience, lastNewBadges.size)
        }

        return response.gainedExperience
    }

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

    suspend fun getSubmittedFeedback(
        userUuid: String,
        sessionUuid: String
    ): Result<List<SubmittedFeedback>> {
        return try {
            val resp = runningApi.getSubmittedFeedback(userUuid, sessionUuid)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRunningCourse(
        userUuid: String,
        courseName: String,
        stats: RunningStats,
        pathPoints: List<LatLng>
    ): Result<RunningCourseResponse> {
        if (pathPoints.size < 2) {
            return Result.failure(IllegalArgumentException("러닝 경로가 부족합니다."))
        }

        val cumulativeDistances = buildCumulativeDistances(pathPoints)
        val gpxBase64 = generateGpxBase64(pathPoints, stats.durationSec)
        val sanitizedFileName = courseName.ifBlank { "course" }
            .replace("\\s+".toRegex(), "_")
            .plus(".gpx")

        val request = RunningCourseRequest(
            userId = userUuid,
            name = courseName,
            distance = stats.distanceKm,
            totalTime = stats.durationSec,
            waypoints = pathPoints.map { WaypointDto(it.latitude, it.longitude) },
            cumulativeDistances = cumulativeDistances,
            gpxFileBase64 = gpxBase64,
            gpxFileName = sanitizedFileName
        )

        return try {
            val response = runningApi.createRunningCourse(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRunningCourses(userUuid: String): Result<List<RunningCourseDto>> {
        return try {
            val response = runningApi.getRunningCourses(userUuid)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildCumulativeDistances(points: List<LatLng>): List<Double> {
        if (points.isEmpty()) return emptyList()

        val result = mutableListOf<Double>()
        var total = 0.0
        points.forEachIndexed { index, latLng ->
            if (index > 0) {
                val previous = points[index - 1]
                total += previous.distanceTo(latLng)
            }
            result.add(total)
        }
        return result
    }

    private fun generateGpxBase64(points: List<LatLng>, durationSeconds: Int): String {
        if (points.isEmpty()) return ""

        val startMillis = System.currentTimeMillis() - durationSeconds * 1000L
        val intervalMillis = if (points.size > 1) {
            durationSeconds * 1000L / (points.size - 1)
        } else {
            0L
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val builder = StringBuilder()
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        builder.append("<gpx version=\"1.1\" creator=\"RunnersHigh\">\n<trk>\n<trkseg>\n")

        points.forEachIndexed { index, point ->
            val time = formatter.format(Date(startMillis + intervalMillis * index))
            builder.append("<trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">")
            builder.append("<time>$time</time></trkpt>\n")
        }

        builder.append("</trkseg>\n</trk>\n</gpx>")

        return Base64.encodeToString(
            builder.toString().toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
    }
}
