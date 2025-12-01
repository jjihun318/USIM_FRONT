package com.example.runnershigh.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnershigh.data.remote.*
import com.example.runnershigh.data.remote.dto.*
import com.example.runnershigh.data.repository.*
import com.example.runnershigh.domain.model.*

import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 러닝 세션의 시작/종료 + 마지막 결과 + 위치/경로를 관리하는 ViewModel
 */
class RunningViewModel : ViewModel() {

    // 🔹 Repository
    private val runningRepository = RunningRepository(ApiClient.runningApi)

    // 🔹 현재 세션 UUID
    private val _currentSessionUuid = MutableStateFlow<String?>(null)
    val currentSessionUuid: StateFlow<String?> = _currentSessionUuid

    // 🔹 결과 / 비교 API 상태
    private val _resultState = MutableStateFlow<SessionResultResponse?>(null)
    val resultState: StateFlow<SessionResultResponse?> = _resultState

    private val _compareState = MutableStateFlow<RunningCompareResponse?>(null)
    val compareState: StateFlow<RunningCompareResponse?> = _compareState

    private val _planGoal = MutableStateFlow(RunningPlanGoal())
    val planGoal: StateFlow<RunningPlanGoal> = _planGoal

    // 🔹 러닝 위치/거리 상태 (지도 & ActiveRunningScreen 에서 사용)
    private val _locationState = MutableStateFlow(RunningLocationState())
    val locationState: StateFlow<RunningLocationState> = _locationState

    // ----------------------------------------------------
    // 세션 시작/종료 API
    // ----------------------------------------------------

    fun startSession(userId: String) {
        viewModelScope.launch {
            val result = runningRepository.startSession(userId)
            result
                .onSuccess { resp ->
                    val sessionId = resp.resolvedSessionId
                    if (sessionId.isNullOrBlank()) {
                        Log.e("RunningVM", "startSession response missing session id: $resp")
                        return@onSuccess
                    }

                    _currentSessionUuid.value = sessionId
                    _compareState.value = null
                    _resultState.value = null
                    _planGoal.value = RunningPlanGoal()
                    Log.d("RunningVM", "session started: $sessionId")

                    // ✅ 세션 시작과 동시에 위치 추적 초기화 + 시작
                    _locationState.value = RunningLocationState(isTracking = true)
                    // ✅ 오늘의 플랜 목표(거리/페이스)를 미리 불러와서 비교에 활용
                    loadRunningComparison(distanceMeters = 0.0, elapsedSeconds = 0)
                }
                .onFailure { e ->
                    Log.e("RunningVM", "startSession failed", e)
                }
        }
    }

    /**
     * 러닝 중에 GPS 포인트를 서버에 올리고 싶을 때 호출
     */
    fun uploadGpsPoint(
        latitude: Double,
        longitude: Double,
        timestamp: String
    ) {
        val sessionId = currentSessionUuid.value ?: return

        viewModelScope.launch {
            val result = runningRepository.uploadGpsPoint(
                sessionUuid = sessionId,
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp
            )

            result
                .onSuccess { resp ->
                    Log.d("RunningVM", "GPS uploaded: ${resp.message}")
                }
                .onFailure { e ->
                    Log.e("RunningVM", "uploadGpsPoint failed", e)
                }
        }
    }

    fun loadRunningComparison(distanceMeters: Double, elapsedSeconds: Int)  {
        val sessionId = currentSessionUuid.value ?: return

        viewModelScope.launch {
            val result = runningRepository.getRunningComparison(sessionId, distanceMeters, elapsedSeconds)
            result
                .onSuccess { resp ->
                    _compareState.value = resp
                    _planGoal.value = RunningPlanGoal(
                        targetDistanceKm = resp.targetDistanceKm,
                        targetPaceSecPerKm = resp.targetPaceSec
                    )
                    Log.d("RunningVM", "compare loaded: $resp")
                }
                .onFailure { e ->
                    Log.e("RunningVM", "getRunningComparison failed", e)
                }
        }
    }

    fun finishSession(stats: RunningStats, userUuid: String) {
        val sessionId = currentSessionUuid.value ?: return

        viewModelScope.launch {
            val result = runningRepository.finishSession(sessionId, userUuid, stats)
            result
                .onSuccess { resp ->
                    Log.d("RunningVM", "finishSession success: $resp")
                }
                .onFailure { e ->
                    Log.e("RunningVM", "finishSession failed", e)
                }
        }

        // ✅ 세션 끝나면 위치 추적도 리셋
        _locationState.value = RunningLocationState()
    }

    fun loadRunningResult() {
        val sessionId = currentSessionUuid.value ?: return

        viewModelScope.launch {
            val result = runningRepository.getRunningResult(sessionId)
            result
                .onSuccess { resp ->
                    _resultState.value = resp
                    Log.d("RunningVM", "getRunningResult: $resp")
                }
                .onFailure { e ->
                    Log.e("RunningVM", "getRunningResult failed", e)
                }
        }
    }

    fun submitFeedback(userUuid: String, feedback: RunningFeedback) {
        val sessionId = currentSessionUuid.value ?: return

        viewModelScope.launch {
            val request = RunningFeedbackRequest(
                sessionId = sessionId,
                userId = userUuid,
                rating = feedback.courseRating,
                difficulty = feedback.difficulty,
                injuryParts = feedback.painAreas,
                comment = feedback.comment
            )

            val result = runningRepository.submitFeedback(request)
            result
                .onSuccess { resp: RunningFeedbackResponse ->
                    Log.d("RunningVM", "feedback submitted: $resp")
                }
                .onFailure { e ->
                    Log.e("RunningVM", "submitFeedback failed", e)
                }
        }
    }

    // ----------------------------------------------------
    // 위치/거리 계산 로직
    // ----------------------------------------------------

    /** Start 버튼 눌렀을 때 호출 (이미 startSession 안에서도 켜고 있지만, 따로 쓰고 싶으면 사용 가능) */
    fun startTracking() {
        _locationState.value = RunningLocationState(isTracking = true)
    }

    /** 정지 버튼 길게 눌렀을 때 등 */
    fun stopTracking() {
        _locationState.update { it.copy(isTracking = false) }
    }

    /**
     * FusedLocation(GPS)에서 새 좌표가 들어올 때마다 호출.
     *  - pathPoints 에 좌표를 추가
     *  - 직전 좌표와의 거리 계산해서 totalDistanceMeters 에 누적
     */
    fun onNewLocation(lat: Double, lng: Double) {
        val state = _locationState.value
        if (!state.isTracking) return

        val newPoint = LatLng(lat, lng)
        val oldPoints = state.pathPoints

        val additionalDistance = if (oldPoints.isNotEmpty()) {
            val lastPoint = oldPoints.last()
            lastPoint.distanceTo(newPoint)   // 단위: 미터
        } else {
            0.0
        }

        val newPath = oldPoints + newPoint
        val newTotal = state.totalDistanceMeters + additionalDistance

        _locationState.value = state.copy(
            pathPoints = newPath,
            totalDistanceMeters = newTotal
        )

        // 🔸 여기서 원하면 동시에 서버로 업로드도 가능
        // val timestamp = ...
        // uploadGpsPoint(lat, lng, timestamp)
    }
}
