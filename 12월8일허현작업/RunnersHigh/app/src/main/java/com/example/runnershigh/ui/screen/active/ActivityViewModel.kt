package com.example.runnershigh.ui.screen.active

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnershigh.data.remote.ApiClient
import com.example.runnershigh.data.remote.api.ActivityApi
import com.example.runnershigh.data.remote.dto.AcquiredBadge
import com.example.runnershigh.data.remote.dto.AcquireBadgeRequest
import com.example.runnershigh.data.remote.dto.BadgeSessionRecord
import com.example.runnershigh.data.remote.dto.ActivityStatsResponse
import com.example.runnershigh.data.remote.dto.RecentActivity
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val DEFAULT_MONTHLY_GOAL_KM = 100.0

data class ActivityUiState(
    val summary: ActivityStatsResponse? = null,
    val total: ActivityStatsResponse? = null,
    val monthlyAverage: ActivityStatsResponse? = null,
    val monthlyHeartZone: ActivityStatsResponse? = null,
    val recent: List<RecentActivity> = emptyList(),
    val newlyAcquiredBadges: List<AcquiredBadge> = emptyList(),
    val achievementRate: Int? = null,
    val conditionLevel: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val currentStats: RunningStats
        get() = RunningStats(
            totalDistanceKm = summary?.monthlySummary?.totalDistance
                ?: total?.monthlySummary?.totalDistance
                ?: 0.0,
            runCount = summary?.monthlySummary?.totalCount
                ?: total?.monthlySummary?.totalCount
                ?: 0,
            avgPace = formatPace(summary?.monthlySummary?.averagePaceSeconds
                ?: total?.monthlySummary?.averagePaceSeconds
                ?: 0),
            avgHeartRate = summary?.monthlySummary?.averageHeartRate
                ?: monthlyAverage?.monthlySummary?.averageHeartRate
                ?: 0
        )

    val dailyData: List<DailyActivityData>
        get() = summary?.dailyGraph
            ?.map { DailyActivityData(day = it.day, distanceKm = it.distance) }
            ?.sortedBy { it.day }
            ?: emptyList()

    val monthlyData: List<MonthlyActivityData>
        get() {
            val month = Calendar.getInstance().get(Calendar.MONTH) + 1
            val distance = summary?.monthlySummary?.totalDistance
                ?: total?.monthlySummary?.totalDistance
                ?: 0.0
            return if (distance > 0.0) listOf(MonthlyActivityData(month = month, distanceKm = distance)) else emptyList()
        }

    val totalData: List<TotalActivityData>
        get() = emptyList()

    val recentActivities: List<RunningData>
        get() = recent.map { activity ->
            RunningData(
                dateLabel = activity.date,
                distanceKm = activity.distance,
                calories = 0
            )
        }

    val goalProgress: Double
        get() {
            val goalKm = DEFAULT_MONTHLY_GOAL_KM
            val progress = (summary?.monthlySummary?.totalDistance ?: 0.0) / goalKm
            return progress.coerceIn(0.0, 1.0)
        }

    private fun parseDay(date: String): Int? = try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsed = formatter.parse(date)
        parsed?.let {
            val cal = Calendar.getInstance().apply { time = it }
            cal.get(Calendar.DAY_OF_MONTH)
        }
    } catch (e: Exception) {
        null
    }

    private fun formatPace(seconds: Int): String {
        if (seconds <= 0) return "-"
        val minutesPart = seconds / 60
        val secondsPart = seconds % 60
        return String.format(Locale.getDefault(), "%d'%02d\"", minutesPart, secondsPart)
    }
}

class ActivityViewModel(
    private val activityApi: ActivityApi = ApiClient.activityApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState(isLoading = true))
    val uiState: StateFlow<ActivityUiState> = _uiState

    fun loadActivityData(userUuid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1

            runCatching { activityApi.getActivitySummary(userUuid, year, month) }
                .onSuccess { summary ->
                    _uiState.value = _uiState.value.copy(summary = summary)
                }
                .onFailure { e -> handleError("Failed to load summary", e) }

            runCatching { activityApi.getActivityTotal(userUuid) }
                .onSuccess { total -> _uiState.value = _uiState.value.copy(total = total) }
                .onFailure { e -> handleError("Failed to load total", e) }

            runCatching { activityApi.getMonthlyAverage(userUuid, year, month) }
                .onSuccess { avg -> _uiState.value = _uiState.value.copy(monthlyAverage = avg) }
                .onFailure { e -> handleError("Failed to load monthly average", e) }

            runCatching { activityApi.getMonthlyHeartZone(userUuid, year, month) }
                .onSuccess { heart -> _uiState.value = _uiState.value.copy(monthlyHeartZone = heart) }
                .onFailure { e -> handleError("Failed to load heart zone", e) }

            runCatching { activityApi.getRecentActivities(userUuid) }
                .onSuccess { recent ->
                    _uiState.value = _uiState.value.copy(recent = recent.recentActivities)
                    checkAndAcquireBadges(userUuid, recent.recentActivities)
                }
                .onFailure { e -> handleError("Failed to load recent activities", e) }

            runCatching { activityApi.getConditionLevel(userUuid) }
                .onSuccess { condition ->
                    _uiState.value = _uiState.value.copy(conditionLevel = condition.conditionLevel)
                }
                .onFailure { e -> handleError("Failed to load condition", e) }

            runCatching { activityApi.getAchievement(userUuid) }
                .onSuccess { achievement ->
                    _uiState.value = _uiState.value.copy(achievementRate = achievement.achievementRate)
                }
                .onFailure { e -> handleError("Failed to load achievement", e) }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun checkAndAcquireBadges(userUuid: String, recentActivities: List<RecentActivity>) {
        val sessionRecords = recentActivities
            .filter { it.sessionId.isNotBlank() }
            .map {
                BadgeSessionRecord(
                    sessionId = it.sessionId,
                    distanceKm = it.distance,
                    durationSec = it.durationSeconds,
                    date = it.date
                )
            }

        if (sessionRecords.isEmpty()) return

        runCatching {
            ApiClient.userService.acquireBadges(
                AcquireBadgeRequest(
                    userUuid = userUuid,
                    sessions = sessionRecords
                )
            )
        }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    val newBadges = response.body()?.newBadges.orEmpty()
                    if (newBadges.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(newlyAcquiredBadges = newBadges)
                    }
                } else {
                    Log.e(
                        "ActivityViewModel",
                        "Badge acquire API failed: ${response.code()} ${response.errorBody()?.string() ?: ""}"
                    )
                }
            }
            .onFailure { e -> handleError("Failed to acquire badges", e) }
    }

    private fun handleError(prefix: String, throwable: Throwable) {
        Log.e("ActivityViewModel", prefix, throwable)
        val friendlyMessage = when (throwable) {
            is HttpException -> {
                val code = throwable.code()
                if (code >= 500) {
                    "서버 오류로 활동 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해주세요."
                } else {
                    "요청을 처리하지 못했습니다 (오류 코드: $code)."
                }
            }

            else -> throwable.localizedMessage ?: "알 수 없는 오류가 발생했습니다."
        }

        val existing = _uiState.value.errorMessage
        if (existing.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = friendlyMessage)
        }
    }
}
