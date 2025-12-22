package com.example.runnershigh.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnershigh.data.remote.*
import com.example.runnershigh.data.remote.dto.*
import com.example.runnershigh.data.repository.*
import com.example.runnershigh.domain.model.*
import com.example.runnershigh.util.GpxManager

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
    private val naverGeocodeRepository = NaverGeocodeRepository(ApiClient.naverGeocodeApi)

    // 🔹 사용자 UUID (세션 결과 조회에 필요)
    private var userUuid: String? = null

    fun updateUserUuid(uuid: String) {
        userUuid = uuid
    }

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

    private val _submittedFeedback = MutableStateFlow<List<SubmittedFeedback>>(emptyList())
    val submittedFeedback: StateFlow<List<SubmittedFeedback>> = _submittedFeedback

    private val _lastRunForCourse = MutableStateFlow<CompletedRunData?>(null)
    val lastRunForCourse: StateFlow<CompletedRunData?> = _lastRunForCourse

    private val _userCourses = MutableStateFlow<List<RunningCourseDto>>(emptyList())
    val userCourses: StateFlow<List<RunningCourseDto>> = _userCourses

    private val _coursesLoading = MutableStateFlow(false)
    val coursesLoading: StateFlow<Boolean> = _coursesLoading

    private val _courseError = MutableStateFlow<String?>(null)
    val courseError: StateFlow<String?> = _courseError

    private val _courseSaveState = MutableStateFlow<CourseSaveState>(CourseSaveState.Idle)
    val courseSaveState: StateFlow<CourseSaveState> = _courseSaveState

    private val _selectedCourse = MutableStateFlow<RunningCourseDto?>(null)
    val selectedCourse: StateFlow<RunningCourseDto?> = _selectedCourse

    fun applyPlanGoalFromPlan(goal: RunningPlanGoal) {
        _planGoal.update { previous ->
            previous.copy(
                targetDistanceKm = goal.targetDistanceKm.takeIf { it > 0 }
                    ?: previous.targetDistanceKm,
                targetPaceSecPerKm = goal.targetPaceSecPerKm ?: previous.targetPaceSecPerKm,
                planTitle = goal.planTitle ?: previous.planTitle
            )
        }
    }

    // 🔹 러닝 위치/거리 상태 (지도 & ActiveRunningScreen 에서 사용)
    private val _locationState = MutableStateFlow(RunningLocationState())
    val locationState: StateFlow<RunningLocationState> = _locationState

    private val _currentRegionLabel = MutableStateFlow<String?>(null)
    val currentRegionLabel: StateFlow<String?> = _currentRegionLabel

    // ----------------------------------------------------
    // 세션 시작/종료 API
    // ----------------------------------------------------

    fun startSession(userId: String) {
        userUuid = userId

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
                    _submittedFeedback.value = emptyList()
                    _courseSaveState.value = CourseSaveState.Idle
                    _lastRunForCourse.value = null
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
                    // 서버에서 내려준 값이 없을 때는 기존 목표를 유지해 사용자가 선택한 플랜을 덮어쓰지 않는다.
                    _planGoal.update { previous ->
                        previous.copy(
                            targetDistanceKm = resp.targetDistanceKm.takeIf { it > 0 }
                                ?: previous.targetDistanceKm,
                            targetPaceSecPerKm = resp.targetPaceSec ?: previous.targetPaceSecPerKm
                        )
                    }
                    Log.d("RunningVM", "compare loaded: $resp")
                }
                .onFailure { e ->
                    Log.e("RunningVM", "getRunningComparison failed", e)
                }
        }
    }

    fun finishSession(stats: RunningStats, userUuid: String) {
        val sessionId = currentSessionUuid.value ?: return
        val goal = planGoal.value
        val goalCompleted = (goal.targetDistanceKm > 0 && stats.distanceKm >= goal.targetDistanceKm) &&
                (goal.targetPaceSecPerKm?.let { stats.paceSecPerKm <= it } ?: true)
        this.userUuid = userUuid

        val locationSnapshot = _locationState.value
        val gpsLogs = locationSnapshot.gpxPoints.map { point ->
            RunningGpsLog(
                latitude = point.latitude,
                longitude = point.longitude,
                timestamp = point.timestampIsoUtc
            )
        }
        _lastRunForCourse.value = CompletedRunData(
            stats = stats,
            pathPoints = locationSnapshot.pathPoints,
            gpxPoints = locationSnapshot.gpxPoints
        )

        viewModelScope.launch {
            val result = runningRepository.finishSession(
                sessionId,
                userUuid,
                stats,
                goal,
                goalCompleted,
                gpsLogs
            )
            result
                .onSuccess { resp ->
                    Log.d("RunningVM", "finishSession success: $resp")
                    loadRunningResult()
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
        val userId = userUuid ?: return
        viewModelScope.launch {
            val result = runningRepository.getRunningResult(sessionId, userId)
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
                userUuid = userUuid,
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

    fun loadSubmittedFeedback(userUuid: String) {
        val sessionId = currentSessionUuid.value ?: return

        viewModelScope.launch {
            val result = runningRepository.getSubmittedFeedback(userUuid, sessionId)
            result
                .onSuccess { feedback ->
                    _submittedFeedback.value = feedback
                    Log.d("RunningVM", "submitted feedback loaded: $feedback")
                }
                .onFailure { e ->
                    Log.e("RunningVM", "loadSubmittedFeedback failed", e)
                }
        }
    }

    fun loadUserCourses(userUuid: String) {
        if (userUuid.isBlank()) return

        viewModelScope.launch {
            _coursesLoading.value = true
            _courseError.value = null

            val result = runningRepository.getRunningCourses(userUuid)
            result
                .onSuccess { courses ->
                    _userCourses.value = courses
                }
                .onFailure { e ->
                    _courseError.value = e.message
                    Log.e("RunningVM", "loadUserCourses failed", e)
                }

            _coursesLoading.value = false
        }
    }

    fun loadRunForCourseFromSession(sessionId: String) {
        if (sessionId.isBlank()) return

        viewModelScope.launch {
            val result = runningRepository.getSessionDetail(sessionId)
            result
                .onSuccess { session ->
                    val validLogs = session.gpsLogs.filter {
                        it.latitude != null && it.longitude != null
                    }
                    if (validLogs.size < 2) {
                        Log.w("RunningVM", "session $sessionId has insufficient gps logs")
                        _lastRunForCourse.value = null
                        _courseSaveState.value = CourseSaveState.Error("최근 러닝 기록의 GPS 로그가 부족합니다.")
                        return@onSuccess
                    }

                    val gpxPoints = validLogs.map { log ->
                        GpxLocationPoint(
                            latitude = log.latitude ?: 0.0,
                            longitude = log.longitude ?: 0.0,
                            elevation = 0.0,
                            timestampIsoUtc = log.timestamp ?: GpxManager.getIso8601Time()
                        )
                    }

                    val stats = RunningStats(
                        distanceKm = session.totalDistanceKm,
                        durationSec = session.totalTimeSec,
                        paceSecPerKm = session.avgPaceSecPerKm,
                        calories = session.calories,
                        avgHeartRate = session.avgHeartRate,
                        elevationGainM = session.elevationGainM,
                        cadence = session.cadence
                    )

                    _lastRunForCourse.value = CompletedRunData(
                        stats = stats,
                        pathPoints = gpxPoints.map { LatLng(it.latitude, it.longitude) },
                        gpxPoints = gpxPoints
                    )
                    _courseSaveState.value = CourseSaveState.Idle
                }
                .onFailure { e ->
                    Log.e("RunningVM", "loadRunForCourseFromSession failed", e)
                }
        }
    }

    fun selectCourse(course: RunningCourseDto) {
        _selectedCourse.value = course

        val targetPace = if (course.distance > 0 && course.totalTime > 0) {
            (course.totalTime / course.distance).toInt()
        } else {
            null
        }

        _planGoal.update { current ->
            current.copy(
                targetDistanceKm = course.distance.takeIf { dist -> dist > 0 } ?: current.targetDistanceKm,
                targetPaceSecPerKm = targetPace ?: current.targetPaceSecPerKm,
                planTitle = if (course.name.isNotBlank()) course.name else current.planTitle
            )
        }
    }

    fun deleteCourse(userUuid: String, courseId: String) {
        if (userUuid.isBlank() || courseId.isBlank()) return

        viewModelScope.launch {
            _coursesLoading.value = true
            _courseError.value = null

            val result = runningRepository.deleteCourse(userUuid, courseId)
            result
                .onSuccess {
                    loadUserCourses(userUuid)
                }
                .onFailure { e ->
                    _courseError.value = e.message ?: "코스 삭제에 실패했습니다."
                    Log.e("RunningVM", "deleteCourse failed", e)
                }

            _coursesLoading.value = false
        }
    }

    fun resetCourseSaveState() {
        _courseSaveState.value = CourseSaveState.Idle
    }

    fun saveCourseFromLastRun(userUuid: String, courseName: String) {
        val latestRun = _lastRunForCourse.value
        if (latestRun == null) {
            _courseSaveState.value = CourseSaveState.Error("최근 러닝 기록을 찾을 수 없습니다.")
            return
        }

        if (courseName.isBlank()) {
            _courseSaveState.value = CourseSaveState.Error("코스 이름을 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _courseSaveState.value = CourseSaveState.Loading
            val result = runningRepository.createRunningCourse(
                userUuid,
                courseName,
                latestRun.stats,
                latestRun.gpxPoints
            )

            result
                .onSuccess { response ->
                    val courseId = response.courseId
                    if (courseId.isNullOrBlank()) {
                        _courseSaveState.value = CourseSaveState.Error("코스 저장 응답에 ID가 없습니다.")
                        Log.e("RunningVM", "saveCourseFromLastRun missing courseId: $response")
                        return@onSuccess
                    }

                    _courseSaveState.value = CourseSaveState.Success(response)
                    Log.d("RunningVM", "course saved: $courseId")
                    loadUserCourses(userUuid)
                }
                .onFailure { e ->
                    val message = e.message ?: "코스 등록에 실패했습니다."
                    _courseSaveState.value = CourseSaveState.Error(message)
                    Log.e("RunningVM", "saveCourseFromLastRun failed", e)
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
    fun onNewLocation(lat: Double, lng: Double, elevationM: Double? = null) {
        val state = _locationState.value
        if (!state.isTracking) return

        val minMovementMeters = 3.0

        val newPoint = LatLng(lat, lng)
        val oldPoints = state.pathPoints
        val newGpxPoint = GpxLocationPoint(
            latitude = lat,
            longitude = lng,
            elevation = elevationM ?: state.currentElevationM,
            timestampIsoUtc = GpxManager.getIso8601Time()
        )

        val rawDistance = if (oldPoints.isNotEmpty()) {
            val lastPoint = oldPoints.last()
            lastPoint.distanceTo(newPoint)   // 단위: 미터
        } else {
            0.0
        }
        val additionalDistance = if (rawDistance >= minMovementMeters) rawDistance else 0.0

        val newPath = oldPoints + newPoint
        val newTotal = state.totalDistanceMeters + additionalDistance
        val newGpxPoints = state.gpxPoints + newGpxPoint

        val currentElevation = elevationM ?: state.currentElevationM
        val gainedElevation = if (elevationM != null && oldPoints.isNotEmpty()) {
            val diff = elevationM - state.currentElevationM
            if (diff > 0) diff else 0.0
        } else {
            0.0
        }

        _locationState.value = state.copy(
            pathPoints = newPath,
            totalDistanceMeters = newTotal,
            currentElevationM = currentElevation,
            totalElevationGainM = state.totalElevationGainM + gainedElevation,
            gpxPoints = newGpxPoints
        )

        // 🔸 여기서 원하면 동시에 서버로 업로드도 가능
        // val timestamp = ...
        // uploadGpsPoint(lat, lng, timestamp)
    }

    fun fetchCurrentRegionLabel(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            runCatching {
                naverGeocodeRepository.fetchRegionLabel(latitude, longitude)
            }.onSuccess { label ->
                _currentRegionLabel.value = label
            }.onFailure { e ->
                Log.e("RunningVM", "fetchCurrentRegionLabel failed", e)
            }
        }
    }
}

data class CompletedRunData(
    val stats: RunningStats,
    val pathPoints: List<LatLng>,
    val gpxPoints: List<GpxLocationPoint>
)

sealed class CourseSaveState {
    object Idle : CourseSaveState()
    object Loading : CourseSaveState()
    data class Success(val response: RunningCourseResponse) : CourseSaveState()
    data class Error(val message: String) : CourseSaveState()
}
