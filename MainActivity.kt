package com.example.capstone

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.launch
import java.net.URL
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// 월별 활동 API 응답 데이터 모델
data class ActivitySummaryResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val totalDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageSteps: Int
)

// 연간 활동 요약 API 응답 모델
data class ActivityYearlyResponse(
    val userId: String,
    val year: Int,
    val totalDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageSteps: Int
)

// 전체 활동 요약 API 응답 모델
data class ActivityTotalResponse(
    val userId: String,
    val totalDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageSteps: Int,
    val startDate: String
)

// 최근 활동 API 응답 모델
data class RecentActivityResponse(
    val activityId: String,
    val date: String,
    val distance: Double,
    val duration: Int,
    val avgHeartRate: Int,
    val calories: Int
)

// 목표 대비 실제 퍼포먼스 API 응답 모델
data class PerformanceComparisonResponse(
    val userId: String,
    val categories: List<PerformanceCategory>
)

data class PerformanceCategory(
    val name: String,
    val target: Int,
    val actual: Int
)

// 목표 대비 실제 퍼포먼스 API 호출 함수
suspend fun fetchPerformanceComparison(userId: String, days: Int = 7): PerformanceComparisonResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/analysis/pace?userId=$userId&days=$days"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val categoriesArray = json.getJSONArray("categories")

            val categories = List(categoriesArray.length()) { i ->
                val item = categoriesArray.getJSONObject(i)
                PerformanceCategory(
                    name = item.getString("name"),
                    target = item.getInt("target"),
                    actual = item.getInt("actual")
                )
            }

            PerformanceComparisonResponse(
                userId = json.getString("userId"),
                categories = categories
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 월별 활동 요약 API 호출 함수
suspend fun fetchActivitySummary(userId: String, year: Int, month: Int): ActivitySummaryResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity_summary_api?userId=user-1234&year=2025&month=1"
            val response = URL(url).readText()
            val json = JSONObject(response)

            ActivitySummaryResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                totalDistance = json.getDouble("totalDistance"),
                runningCount = json.getInt("runningCount"),
                averagePace = json.getString("averagePace"),
                averageSteps = json.getInt("averageSteps")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 연간 활동 요약 API 호출 함수
suspend fun fetchActivityYearly(userId: String, year: Int): ActivityYearlyResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity_yearly_api?userId=user-1234&year=2025"
            val response = URL(url).readText()
            val json = JSONObject(response)

            ActivityYearlyResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                totalDistance = json.getDouble("totalDistance"),
                runningCount = json.getInt("runningCount"),
                averagePace = json.getString("averagePace"),
                averageSteps = json.getInt("averageSteps")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 전체 활동 요약 API 호출 함수
suspend fun fetchActivityTotal(userId: String): ActivityTotalResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity_total_activity_api?userId=user-1234"
            val response = URL(url).readText()
            val json = JSONObject(response)

            ActivityTotalResponse(
                userId = json.getString("userId"),
                totalDistance = json.getDouble("totalDistance"),
                runningCount = json.getInt("runningCount"),
                averagePace = json.getString("averagePace"),
                averageSteps = json.getInt("averageSteps"),
                startDate = json.getString("startDate")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 최근 활동 목록 API 호출 함수
suspend fun fetchRecentActivities(userId: String, limit: Int = 5): List<RecentActivityResponse>? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity_recent_api?userId=user-1234&limit=5"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val activitiesArray = json.getJSONArray("activities")

            List(activitiesArray.length()) { i ->
                val item = activitiesArray.getJSONObject(i)
                RecentActivityResponse(
                    activityId = item.getString("activityId"),
                    date = item.getString("date"),
                    distance = item.getDouble("distance"),
                    duration = item.getInt("duration"),
                    avgHeartRate = item.getInt("avgHeartRate"),
                    calories = item.getInt("calories")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 월간 평균 통계 API 호출 함수
suspend fun fetchMonthlyAverage(userId: String, year: Int, month: Int): MonthlyAverageResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1//activity/summary/total?userId={userId}"
            val response = URL(url).readText()
            val json = JSONObject(response)

            MonthlyAverageResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                averageDistance = json.getDouble("averageDistance"),
                runningCount = json.getInt("runningCount"),
                averagePace = json.getString("averagePace"),
                averageHeartRate = json.getInt("averageHeartRate")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 심박수 구간 분석 API 호출 함수
suspend fun fetchMonthlyHeartZone(userId: String, year: Int, month: Int): MonthlyHeartZoneResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://localhost:5001/runners-high-capstone/us-central1/activity_monthly_heart_zone_api?userId=user-1234&year=2025&month=1"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val zonesArray = json.getJSONArray("zones")

            val zones = List(zonesArray.length()) { i ->
                val zone = zonesArray.getJSONObject(i)
                HeartRateZone(
                    zoneName = zone.getString("zoneName"),
                    minBpm = zone.getInt("minBpm"),
                    maxBpm = zone.getInt("maxBpm"),
                    percentage = zone.getInt("percentage")
                )
            }

            MonthlyHeartZoneResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                zones = zones
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 마라톤 피드백 API 호출 함수
suspend fun fetchMarathonFeedback(userId: String, year: Int, month: Int): MarathonFeedbackResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity/marathon-feedback?userId=$userId&year=$year&month=$month"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val feedbacksArray = json.getJSONArray("feedbacks")

            val feedbacks = List(feedbacksArray.length()) { i ->
                val feedback = feedbacksArray.getJSONObject(i)
                MarathonFeedbackScore(
                    category = feedback.getString("category"),
                    score = feedback.getInt("score"),
                    feedback = feedback.getString("feedback")
                )
            }

            MarathonFeedbackResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                feedbacks = feedbacks
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 향상 제안 API 호출 함수
suspend fun fetchMonthlySuggestions(userId: String, year: Int, month: Int): MonthlySuggestionsResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity/monthly-suggestions?userId=$userId&year=$year&month=$month"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val suggestionsArray = json.getJSONArray("suggestions")

            val suggestions = List(suggestionsArray.length()) { i ->
                suggestionsArray.getString(i)
            }

            MonthlySuggestionsResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                suggestions = suggestions
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 전체 평가 API 호출 함수
suspend fun fetchMonthlyOverall(userId: String, year: Int, month: Int): MonthlyOverallResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity/monthly-overall?userId=$userId&year=$year&month=$month"
            val response = URL(url).readText()
            val json = JSONObject(response)

            MonthlyOverallResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                overallEvaluation = json.getString("overallEvaluation")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 부상 부위 분석 API 호출 함수
suspend fun fetchInjuryAnalysis(userId: String): InjuryAnalysisResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/analysis/injury?userId=$userId"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val injuriesArray = json.getJSONArray("injuries")

            val injuries = List(injuriesArray.length()) { i ->
                val item = injuriesArray.getJSONObject(i)
                InjuryAnalysisItem(
                    part = item.getString("part"),
                    percentage = item.getInt("percentage"),
                    count = item.getInt("count")
                )
            }

            InjuryAnalysisResponse(
                userId = json.getString("userId"),
                injuries = injuries,
                totalCount = json.getInt("totalCount")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 페이스 하락 구간 분석 API 호출 함수
suspend fun fetchPaceDropAnalysis(sessionId: String): PaceDropAnalysisResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/analysis/pace-drop?sessionId=$sessionId"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val sectionsArray = json.getJSONArray("sections")

            val sections = List(sectionsArray.length()) { i ->
                val section = sectionsArray.getJSONObject(i)
                PaceDropSection(
                    distance = section.getString("distance"),
                    avgPace = section.getString("avgPace"),
                    paceChange = if (section.has("paceChange")) section.getString("paceChange") else null,
                    status = section.getString("status"),
                    severity = section.getString("severity")
                )
            }

            PaceDropAnalysisResponse(
                sessionId = json.getString("sessionId"),
                sections = sections
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 심박수 변동성(HRV) API 호출 함수
suspend fun fetchHRVStats(sessionId: String): HRVStatsResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/hrv_stats_api?sessionId=session-1234"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val dailyArray = json.getJSONArray("dailyHRV")

            val dailyHRV = List(dailyArray.length()) { i ->
                val daily = dailyArray.getJSONObject(i)
                DailyHRV(
                    date = daily.getString("date"),
                    hrv = daily.getInt("hrv")
                )
            }

            HRVStatsResponse(
                sessionId = json.getString("sessionId"),
                avgHRV = json.getInt("avgHRV"),
                minHRV = json.getInt("minHRV"),
                maxHRV = json.getInt("maxHRV"),
                trend = json.getString("trend"),
                dailyHRV = dailyHRV
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 맞춤형 피드백 API 호출 함수
suspend fun fetchCustomFeedback(userId: String, sessionId: String): CustomFeedbackResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/analysis/feedback?userId=$userId&sessionId=$sessionId"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val feedbacksArray = json.getJSONArray("feedbacks")

            val feedbacks = List(feedbacksArray.length()) { i ->
                val feedback = feedbacksArray.getJSONObject(i)
                val tipsArray = feedback.getJSONArray("tips")
                val tips = List(tipsArray.length()) { j ->
                    tipsArray.getString(j)
                }

                FeedbackItem(
                    category = feedback.getString("category"),
                    title = feedback.getString("title"),
                    description = feedback.getString("description"),
                    tips = tips
                )
            }

            CustomFeedbackResponse(
                userId = json.getString("userId"),
                sessionId = json.getString("sessionId"),
                feedbacks = feedbacks
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 주간 컨디션 점수 API 호출 함수
suspend fun fetchWeeklyCondition(userId: String, year: Int, month: Int): WeeklyConditionResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/analysis/weekly-condition?userId=$userId&year=$year&month=$month"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val scoresArray = json.getJSONArray("weeklyScores")

            val scores = List(scoresArray.length()) { i ->
                val scoreObj = scoresArray.getJSONObject(i)
                WeeklyConditionScore(
                    week = scoreObj.getString("week"),
                    score = scoreObj.getInt("score")
                )
            }

            WeeklyConditionResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                weeklyScores = scores
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 종합 컨디션 분석(종합 평가) API 호출 함수
suspend fun fetchOverallCondition(userId: String): OverallConditionResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/overall_condition_api?userId=$userId"
            val response = URL(url).readText()
            val json = JSONObject(response)

            OverallConditionResponse(
                userId = json.getString("userId"),
                conditionLevel = json.getInt("conditionLevel"),
                evaluation = json.getString("evaluation")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 일별 활동 그래프 API 호출 함수
suspend fun fetchDailyActivity(userId: String, year: Int, month: Int): DailyActivityResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity_daily_api?userId=user-1234&year=2025&month=1"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val activitiesArray = json.getJSONArray("activities")

            val activities = List(activitiesArray.length()) { i ->
                val activity = activitiesArray.getJSONObject(i)
                DailyActivityData(
                    day = activity.getInt("day"),
                    distance = activity.getDouble("distance")
                )
            }

            DailyActivityResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                activities = activities
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 월별 활동 그래프 API 호출 함수
suspend fun fetchMonthlyActivity(userId: String, year: Int): MonthlyActivityResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity_monthly_graph_api?userId=user-1234&year=2025"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val activitiesArray = json.getJSONArray("activities")

            val activities = List(activitiesArray.length()) { i ->
                val activity = activitiesArray.getJSONObject(i)
                MonthlyActivityData(
                    month = activity.getInt("month"),
                    distance = activity.getDouble("distance")
                )
            }

            MonthlyActivityResponse(
                userId = json.getString("userId"),
                year = json.getInt("year"),
                activities = activities
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 전체 활동량 그래프 데이터 조회 API 호출 함수
suspend fun fetchTotalActivity(userId: String): TotalActivityResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "http://127.0.0.1:5001/runners-high-capstone/us-central1/activity_total_activity_api?userId=user-1234"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val activitiesArray = json.getJSONArray("activities")

            val activities = List(activitiesArray.length()) { i ->
                val activity = activitiesArray.getJSONObject(i)
                TotalActivityData(
                    year = activity.getInt("year"),
                    month = activity.getInt("month"),
                    distance = activity.getDouble("distance")
                )
            }

            TotalActivityResponse(
                userId = json.getString("userId"),
                activities = activities
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 컨디션 레벨 API 응답 모델
data class ConditionLevelResponse(
    val userId: String,
    val conditionLevel: Int,
    val analysis: String,
    val lastUpdated: String
)

// 월간 평균 통계 API 응답 모델
data class MonthlyAverageResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val averageDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageHeartRate: Int
)

// 심박수 구간 분석 API 응답 모델
data class HeartRateZone(
    val zoneName: String,
    val minBpm: Int,
    val maxBpm: Int,
    val percentage: Int
)

data class MonthlyHeartZoneResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val zones: List<HeartRateZone>
)

// 마라톤 피드백 API 응답 모델
data class MarathonFeedbackScore(
    val category: String,
    val score: Int,
    val feedback: String
)

data class MarathonFeedbackResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val feedbacks: List<MarathonFeedbackScore>
)

// 향상 제안 API 응답 모델
data class MonthlySuggestionsResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val suggestions: List<String>
)

// 전체 평가 API 응답 모델
data class MonthlyOverallResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val overallEvaluation: String
)

// 부상 부위 분석 API 응답 모델
data class InjuryAnalysisItem(
    val part: String,
    val percentage: Int,
    val count: Int
)

data class InjuryAnalysisResponse(
    val userId: String,
    val injuries: List<InjuryAnalysisItem>,
    val totalCount: Int
)

// 페이스 하락 구간 분석 API 응답 모델
data class PaceDropSection(
    val distance: String,
    val avgPace: String,
    val paceChange: String?,
    val status: String,
    val severity: String
)

data class PaceDropAnalysisResponse(
    val sessionId: String,
    val sections: List<PaceDropSection>
)

// HRV 통계(그래프) API 응답 모델
data class HRVStatsResponse(
    val sessionId: String,
    val avgHRV: Int,
    val minHRV: Int,
    val maxHRV: Int,
    val trend: String,
    val dailyHRV: List<DailyHRV>
)

data class DailyHRV(
    val date: String,
    val hrv: Int
)

// 맞춤형 피드백 API 응답 모델
data class FeedbackItem(
    val category: String,
    val title: String,
    val description: String,
    val tips: List<String>
)

data class CustomFeedbackResponse(
    val userId: String,
    val sessionId: String,
    val feedbacks: List<FeedbackItem>
)

// 주간 컨디션 점수 API 응답 모델
data class WeeklyConditionResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val weeklyScores: List<WeeklyConditionScore>
)

// 종합 컨디션 분석 API 응답 모델
data class OverallConditionResponse(
    val userId: String,
    val conditionLevel: Int,
    val evaluation: String
)

// 일별 활동 그래프 API 응답 모델
data class DailyActivityData(
    val day: Int,
    val distance: Double
)

data class DailyActivityResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val activities: List<DailyActivityData>
)

// 월별 활동 그래프 API 응답 모델
data class MonthlyActivityData(
    val month: Int,
    val distance: Double
)

data class MonthlyActivityResponse(
    val userId: String,
    val year: Int,
    val activities: List<MonthlyActivityData>
)

// 전체 활동 타임라인 API 응답 모델
data class TotalActivityData(
    val year: Int,
    val month: Int,
    val distance: Double
)

data class TotalActivityResponse(
    val userId: String,
    val activities: List<TotalActivityData>
)

data class HRVData(
    val date: String,
    val value: Int
)

// 페이스 비교 데이터
data class PaceComparisonData(
    val date: String,
    val targetPace: Int, // 초 단위
    val actualPace: Int  // 초 단위
)

// 심박수 구간 데이터
data class HeartRateZoneData(
    val zoneName: String,
    val range: String,
    val percentage: Int,
    val color: Color
)

// 목표별 피드백 카테고리
data class GoalFeedbackItem(
    val icon: String,
    val title: String,
    val score: Int,
    val description: String,
    val color: Color
)

sealed class Screen {
    object Main : Screen()
    object GoalDetail : Screen()
    object ConditionDetail : Screen()
}

// 러닝 데이터 모델
data class RunningData(
    val date: LocalDate,
    val distance: Double,
    val duration: Int,
    val avgHeartRate: Int,
    val calories: Int
)

enum class PeriodType { ALL, YEAR, MONTH }

data class RunningStats(
    val totalDistance: Double,
    val runCount: Int,
    val avgPace: String,
    val avgHeartRate: Int
)

// 컨디션 데이터
data class InjuryData(
    val part: String,
    val percentage: Int,
    val hosoCount: Int = 0,
    val severity: String = "보통"
)

data class PaceDeclineData(
    val distance: String,
    val severity: SeverityLevel,
    val avgPace: String? = null,
    val paceChange: String? = null
)

enum class SeverityLevel {
    LOW, MEDIUM, HIGH
}

data class WeeklyConditionScore(
    val week: String,
    val score: Int
)

// 사용자 목표
enum class UserGoal(val displayName: String) {
    MARATHON("마라톤 준비"),
    DIET("다이어트"),
    FITNESS("격투기, 체력 증진"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

            // 뒤로가기 처리
            BackHandler(enabled = currentScreen != Screen.Main) {
                currentScreen = Screen.Main
            }

            when (currentScreen) {
                Screen.Main -> FitnessScreen(
                    onGoalClick = { currentScreen = Screen.GoalDetail },
                    onConditionClick = { currentScreen = Screen.ConditionDetail }
                )
                Screen.GoalDetail -> GoalDetailScreen(
                    onBackClick = { currentScreen = Screen.Main }
                )
                Screen.ConditionDetail -> ConditionDetailScreen(
                    onBackClick = { currentScreen = Screen.Main }
                )
            }
        }
    }
}

// 새로운 데이터 모델
data class FeedbackCategory(
    val icon: String,
    val color: Color,
    val backgroundColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val tips: List<String>
)

@Composable
fun ImprovementSection(customFeedback: CustomFeedbackResponse?) {
    val feedbackCategories = customFeedback?.feedbacks?.map { feedback ->
        val (icon, color, backgroundColor) = when (feedback.category) {
            "부상 예방" -> Triple("⚠️", Color(0xFFF44336), Color(0xFFFFEBEE))
            "페이스 관리" -> Triple("⚡", Color(0xFFFF9800), Color(0xFFFFF3E0))
            "회복력" -> Triple("💙", Color(0xFF2196F3), Color(0xFFE3F2FD))
            else -> Triple("📊", Color(0xFF9E9E9E), Color(0xFFF5F5F5))
        }

        FeedbackCategory(
            icon = icon,
            color = color,
            backgroundColor = backgroundColor,
            title = feedback.category,
            subtitle = feedback.title,
            description = feedback.description,
            tips = feedback.tips
        )
    } ?: emptyList()

    Column {
        Text(
            text = "맞춤형 피드백",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (feedbackCategories.isEmpty()) {
            Text(
                text = "피드백 데이터를 불러오는 중...",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        } else {
            feedbackCategories.forEach { category ->
                FeedbackCard(category = category)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun FeedbackCard(category: FeedbackCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = category.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = category.icon,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = category.title,
                        fontSize = 14.sp,
                        color = category.color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = category.subtitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.description,
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 14.sp
                        )
                        Text(
                            text = category.tips[0],
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    category.tips.drop(1).forEach { tip ->
                        Text(
                            text = tip,
                            fontSize = 12.sp,
                            color = Color(0xFF424242),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HRVSection(hrvData: List<HRVData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "심박수 변동성 (HRV)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "높을수록 회복력이 좋습니다",
                        fontSize = 13.sp,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HRVLineChart(
                        data = hrvData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentHRV = hrvData.lastOrNull()?.value ?: 0
            val status = when {
                currentHRV >= 70 -> "우수"
                currentHRV >= 50 -> "양호"
                currentHRV >= 30 -> "보통"
                else -> "주의"
            }

            Text(
                text = "현재 HRV: $currentHRV ($status)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
fun HRVLineChart(data: List<HRVData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val chartWidth = size.width - 80f
        val chartHeight = size.height - 40f

        // 데이터가 없으면 기본값 사용
        if (data.isEmpty()) {
            drawContext.canvas.nativeCanvas.drawText(
                "데이터 없음",
                size.width / 2,
                size.height / 2,
                android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 40f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
            return@Canvas
        }

        // 동적으로 최대/최소값 계산
        val maxValue = (data.maxOfOrNull { it.value }?.toFloat() ?: 100f) + 10f
        val minValue = (data.minOfOrNull { it.value }?.toFloat() ?: 0f) - 10f
        val valueRange = maxValue - minValue

        // Y축 그리드 라인 및 레이블
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = (minValue + (valueRange * i / 4)).toInt()

            // 그리드 라인
            drawLine(
                color = Color(0xFFBBDEFB),
                start = Offset(60f, y),
                end = Offset(chartWidth + 60f, y),
                strokeWidth = 1f
            )

            // Y축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                20f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = Color(0xFF1976D2).toArgb()
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // 데이터 포인트 계산
        val points = data.mapIndexed { index, hrvData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            val normalizedValue = (hrvData.value - minValue) / valueRange
            val y = chartHeight - (chartHeight * normalizedValue) + 20f
            Offset(x, y)
        }

        // 라인 그리기
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF4CAF50),
                style = Stroke(width = 6f)
            )
        }

        // 데이터 포인트 그리기
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 8f,
                center = point
            )
        }

        // X축 레이블
        data.forEachIndexed { index, hrvData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            drawContext.canvas.nativeCanvas.drawText(
                hrvData.date,
                x,
                chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = Color(0xFF1976D2).toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionDetailScreen(onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // API 데이터 상태 추가
    var hrvStats by remember { mutableStateOf<HRVStatsResponse?>(null) }
    var customFeedback by remember { mutableStateOf<CustomFeedbackResponse?>(null) }
    var weeklyCondition by remember { mutableStateOf<WeeklyConditionResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var overallCondition by remember { mutableStateOf<OverallConditionResponse?>(null) }
    var injuryAnalysis by remember { mutableStateOf<InjuryAnalysisResponse?>(null) }
    var paceDropAnalysis by remember { mutableStateOf<PaceDropAnalysisResponse?>(null) }

    val today = LocalDate.now()

    // API 호출
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userId = "user-1234" // 실제 사용자 ID로 변경 필요
            val sessionId = "session-1234" // 실제 세션 ID로 변경 필요
            hrvStats = fetchHRVStats(sessionId)
            customFeedback = fetchCustomFeedback(userId, sessionId)
            weeklyCondition = fetchWeeklyCondition(userId, today.year, today.monthValue)
            overallCondition = fetchOverallCondition(userId)
            injuryAnalysis = fetchInjuryAnalysis(userId)
            paceDropAnalysis = fetchPaceDropAnalysis(sessionId)
            isLoading = false
        }
    }

    // 선택된 기간
    var selectedPeriod by remember { mutableStateOf(PeriodType.ALL) }

    // 컨디션 레벨 (API에서 가져오기)
    val conditionLevel = remember(overallCondition) {
        overallCondition?.conditionLevel ?: 0
    }

    // 부상 데이터 (API에서 변환)
    val injuryData = remember(injuryAnalysis, selectedPeriod) {
        injuryAnalysis?.injuries?.map { item ->
            InjuryData(
                part = item.part,
                percentage = item.percentage,
                hosoCount = item.count,
                severity = when {
                    item.percentage >= 30 -> "주의"
                    item.percentage >= 15 -> "보통"
                    else -> "양호"
                }
            )
        } ?: emptyList()
    }

    // 페이스 하락 구간 (API에서 변환)
    val paceDeclineData = remember(paceDropAnalysis) {
        paceDropAnalysis?.sections?.map { section ->
            val severity = when (section.severity) {
                "high" -> SeverityLevel.HIGH
                "medium" -> SeverityLevel.MEDIUM
                else -> SeverityLevel.LOW
            }
            PaceDeclineData(
                distance = section.distance,
                avgPace = section.avgPace,
                paceChange = section.paceChange,
                severity = severity
            )
        } ?: emptyList()
    }

    // 주간 컨디션 점수 (API에서 가져오기)
    val weeklyScores = remember(weeklyCondition) {
        weeklyCondition?.weeklyScores ?: emptyList()
    }

    // API 데이터 변환
    val hrvData = remember(hrvStats) {
        hrvStats?.dailyHRV?.map { daily ->
            // 날짜 형식 변환: "2025-10-12" -> "10/12"
            val dateParts = daily.date.split("-")
            val formattedDate = if (dateParts.size >= 3) {
                "${dateParts[1]}/${dateParts[2]}"
            } else {
                daily.date
            }
            HRVData(
                date = formattedDate,
                value = daily.hrv
            )
        } ?: emptyList()
    }

    // 컨디션 히스토리 (그래프용)
    val conditionHistory = remember(weeklyCondition) {
        weeklyCondition?.weeklyScores?.map { score ->
            WeeklyConditionScore(
                week = score.week,
                score = score.score
            )
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바
        TopAppBar(
            title = { Text("컨디션 레벨 분석") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFC8E6C9),
                titleContentColor = Color(0xFF1B5E20)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 로딩 상태 표시
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF558B2F))
                }
            } else {
                // 컨디션 레벨 지수
                ConditionScoreCard(
                    score = conditionLevel,
                    conditionHistory = conditionHistory
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 기간 선택
                Text(
                    text = "주요 통증 호소 부위",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 부상 부위 통계
                InjuryStatsCard(injuryData = injuryData)
                Spacer(modifier = Modifier.height(16.dp))

                // 페이스 하락 구간
                PaceDeclineSection(paceDeclineData = paceDeclineData)
                Spacer(modifier = Modifier.height(16.dp))

                HRVSection(hrvData = hrvData)
                Spacer(modifier = Modifier.height(16.dp))

                // 향상 제안
                ImprovementSection(customFeedback = customFeedback)
                Spacer(modifier = Modifier.height(16.dp))

                // 주간 컨디션 점수
                WeeklyConditionSection(weeklyScores = weeklyScores)
                Spacer(modifier = Modifier.height(16.dp))

                // 종합 평가
                ComprehensiveEvaluation(
                    evaluationText = overallCondition?.evaluation ?: "평가 정보를 불러오는 중입니다."
                )
            }
        }
    }
}

@Composable
fun ConditionLevelCard(conditionLevel: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // 높이 증가
            .clickable(onClick = onClick), // 클릭 기능 유지
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "컨디션 레벨 지수",
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = conditionLevel.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = " / 100",
                    fontSize = 14.sp,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

data class ImprovementSuggestion(
    val title: String,
    val description: String,
    val severity: SeverityLevel
)

@Composable
fun ConditionScoreCard(score: Int, conditionHistory: List<WeeklyConditionScore>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "컨디션 레벨 지수",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "추세",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = score.toString(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = " / 100",
                    fontSize = 16.sp,
                    color = Color(0xFF558B2F),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = "건강한 러닝을 유지하고 있습니다 ✨",
                fontSize = 13.sp,
                color = Color(0xFF558B2F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 데이터 없을 때 처리 추가
            if (conditionHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "데이터 없음",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            } else {
                ConditionLineChart(
                    data = conditionHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
fun ConditionLineChart(data: List<WeeklyConditionScore>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val chartWidth = size.width - 80f
        val chartHeight = size.height - 40f
        val maxValue = 100f
        val minValue = 0f

        // Y축 그리드 라인 및 레이블 (0, 25, 50, 75, 100)
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = i * 25

            // 그리드 라인
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(60f, y),
                end = Offset(chartWidth + 60f, y),
                strokeWidth = 1f
            )

            // Y축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                20f,
                y + 10f,
                Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 30f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        // 데이터 포인트 계산
        val points = data.mapIndexed { index, scoreData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            val normalizedValue = (scoreData.score - minValue) / (maxValue - minValue)
            val y = chartHeight - (chartHeight * normalizedValue) + 20f
            Offset(x, y)
        }

        // 라인 그리기
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF2196F3),
                style = Stroke(width = 6f)
            )
        }

        // 데이터 포인트 그리기
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF2196F3),
                radius = 8f,
                center = point
            )
        }

        // X축 레이블 (날짜)
        data.forEachIndexed { index, scoreData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            val displayDate = scoreData.week.replace("주 전", "").replace("이번 주", "10/18")
            drawContext.canvas.nativeCanvas.drawText(
                displayDate,
                x,
                chartHeight + 50f,
                Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun InjuryStatsCard(injuryData: List<InjuryData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (injuryData.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "데이터 없음",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        } else {
            injuryData.forEach { injury ->
                InjuryProgressBar(injury)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InjuryProgressBar(injury: InjuryData) {
    val backgroundColor = when (injury.severity) {
        "주의" -> Color(0xFFFFF3E0)
        "보통" -> Color(0xFFE3F2FD)
        else -> Color(0xFFF5F5F5)
    }

    val progressColor = when (injury.severity) {
        "주의" -> Color(0xFFFF9800)
        "보통" -> Color(0xFF2196F3)
        else -> Color(0xFF9E9E9E)
    }

    val severityColor = when (injury.severity) {
        "주의" -> Color(0xFFFF9800)
        "보통" -> Color(0xFF4CAF50)
        else -> Color(0xFF9E9E9E)
    }

    val icon = when (injury.part) {
        "무릎" -> "🦵"
        "발목" -> "🦶"
        "허벅지" -> "🦵"
        "종아리" -> "🦵"
        "발바닥" -> "🦶"
        "정강이" -> "🦵"
        else -> "👤"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            // 중간 내용
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = injury.part,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${injury.percentage}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = severityColor)
                        ) {
                            Text(
                                text = injury.severity,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${injury.hosoCount}회 호소",
                    fontSize = 13.sp,
                    color = Color(0xFF558B2F)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = injury.percentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = progressColor,
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

@Composable
fun PaceDeclineSection(paceDeclineData: List<PaceDeclineData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "페이스 하락 구간",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (paceDeclineData.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "데이터 없음",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        } else {
            paceDeclineData.forEach { data ->
                PaceDeclineCard(data = data)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PaceDeclineCard(data: PaceDeclineData) {
    val (statusText, statusColor, borderColor) = when (data.severity) {
        SeverityLevel.LOW -> Triple("정상", Color(0xFF4CAF50), Color(0xFF4CAF50))
        SeverityLevel.MEDIUM -> Triple("주의", Color(0xFFFF9800), Color(0xFFFF9800))
        SeverityLevel.HIGH -> Triple("개선 필요", Color(0xFFF44336), Color(0xFFF44336))
    }

    // 실제 평균 페이스 기록이 있으면 그걸 쓰고, 없으면 운동 강도(또는 상태)에 맞춰서 적절한 추정치를 대신 쓴다
    val pace = data.avgPace ?: when (data.severity) {
        SeverityLevel.LOW -> "5'48\""
        SeverityLevel.MEDIUM -> "6'02\""
        SeverityLevel.HIGH -> "6'28\""
    }
    val paceChange = data.paceChange

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 거리 구간
            Column {
                Text(
                    text = data.distance,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "평균: $pace",
                    fontSize = 13.sp,
                    color = Color(0xFF558B2F)
                )
            }

            // 오른쪽: 상태
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                paceChange?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color(0xFF558B2F)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyConditionSection(weeklyScores: List<WeeklyConditionScore>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "주간 컨디션 점수",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            weeklyScores.forEach { score ->
                WeeklyScoreItem(score = score)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun WeeklyScoreItem(score: WeeklyConditionScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = score.week,
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = score.score.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
        }
    }
}

@Composable
fun ComprehensiveEvaluation(evaluationText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "종합 평가",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = evaluationText,
                fontSize = 14.sp,
                color = Color(0xFFE8F5E9),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ActivityGraphSection(
    selectedPeriod: PeriodType,
    dailyData: List<DailyActivityData>?,
    monthlyData: List<MonthlyActivityData>?,
    totalData: List<TotalActivityData>?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = when (selectedPeriod) {
                    PeriodType.ALL -> "전체 활동"
                    PeriodType.YEAR -> "월별 활동"
                    PeriodType.MONTH -> "일별 활동"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedPeriod) {
                PeriodType.MONTH -> {
                    if (dailyData.isNullOrEmpty()) {
                        NoDataMessage()
                    } else {
                        DailyActivityChart(
                            data = dailyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
                PeriodType.YEAR -> {
                    if (monthlyData.isNullOrEmpty()) {
                        NoDataMessage()
                    } else {
                        MonthlyActivityChart(
                            data = monthlyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
                PeriodType.ALL -> {
                    if (totalData.isNullOrEmpty()) {
                        NoDataMessage()
                    } else {
                        TotalActivityChart(
                            data = totalData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoDataMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "데이터 없음",
            fontSize = 16.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

@Composable
fun DailyActivityChart(data: List<DailyActivityData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 100f
        val chartHeight = size.height - 80f
        val maxValue = 50f // 0~50km
        val barWidth = (chartWidth / 31f) * 0.7f // 최대 31일

        // Y축 그리드 (0, 10, 20, 30, 40, 50)
        for (i in 0..5) {
            val y = chartHeight - (chartHeight * i / 5f) + 20f
            val value = i * 10

            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(80f, y),
                end = Offset(chartWidth + 80f, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                "${value}km",
                40f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // 막대 그래프
        data.forEach { activity ->
            val x = 80f + (chartWidth * (activity.day - 1) / 30f)
            val barHeight = ((activity.distance / maxValue) * chartHeight).toFloat()  // toFloat() 추가
            val barY = chartHeight - barHeight + 20f

            drawRect(
                color = Color(0xFF558B2F),
                topLeft = Offset(x, barY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }

        // X축 레이블 (5일 단위)
        for (i in 1..31 step 5) {
            val x = 80f + (chartWidth * (i - 1) / 30f)
            drawContext.canvas.nativeCanvas.drawText(
                i.toString(),
                x + barWidth / 2,
                chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun MonthlyActivityChart(data: List<MonthlyActivityData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 100f
        val chartHeight = size.height - 80f
        val maxValue = 100f // 0~100km
        val barWidth = (chartWidth / 12f) * 0.7f

        // Y축 그리드 (0, 25, 50, 75, 100)
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = i * 25

            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(80f, y),
                end = Offset(chartWidth + 80f, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                "${value}km",
                40f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // 막대 그래프
        data.forEach { activity ->
            val x = 80f + (chartWidth * (activity.month - 1) / 11f)
            val barHeight = ((activity.distance / maxValue) * chartHeight).toFloat()  // toFloat() 추가
            val barY = chartHeight - barHeight + 20f

            drawRect(
                color = Color(0xFF558B2F),
                topLeft = Offset(x, barY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }

        // X축 레이블 (모든 월)
        for (i in 1..12) {
            val x = 80f + (chartWidth * (i - 1) / 11f)
            drawContext.canvas.nativeCanvas.drawText(
                "${i}월",
                x + barWidth / 2,
                chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun TotalActivityChart(data: List<TotalActivityData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 120f
        val chartHeight = size.height - 80f
        val maxValue = 100f
        val barWidth = if (data.isNotEmpty()) (chartWidth / data.size) * 0.7f else 20f

        // Y축 그리드 (0, 25, 50, 75, 100)
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = i * 25

            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(100f, y),
                end = Offset(chartWidth + 100f, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                "${value}km",
                50f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // 막대 그래프
        data.forEachIndexed { index, activity ->
            val x = 100f + (chartWidth * index / data.size.coerceAtLeast(1))
            val barHeight = ((activity.distance / maxValue) * chartHeight).toFloat()  // toFloat() 추가
            val barY = chartHeight - barHeight + 20f

            drawRect(
                color = Color(0xFF558B2F),
                topLeft = Offset(x, barY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }

        // X축 레이블 (년/월)
        data.forEachIndexed { index, activity ->
            val x = 100f + (chartWidth * index / data.size.coerceAtLeast(1))
            val label = "${activity.year % 100}/${activity.month}"
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barWidth / 2,
                chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(onGoalClick: () -> Unit, onConditionClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // 현재 날짜를 가져옴
    val today = remember { LocalDate.now() }

    // API 데이터 상태
    var activitySummary by remember { mutableStateOf<ActivitySummaryResponse?>(null) }
    var activityYearly by remember { mutableStateOf<ActivityYearlyResponse?>(null) }
    var activityTotal by remember { mutableStateOf<ActivityTotalResponse?>(null) }
    var recentActivities by remember { mutableStateOf<List<RecentActivityResponse>?>(null) }
    var dailyActivity by remember { mutableStateOf<DailyActivityResponse?>(null) }
    var monthlyActivity by remember { mutableStateOf<MonthlyActivityResponse?>(null) }
    var totalActivity by remember { mutableStateOf<TotalActivityResponse?>(null) }
    var conditionLevel by remember { mutableStateOf<ConditionLevelResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // API 호출
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userId = "user-1234"
            val year = today.year
            val month = today.monthValue

            activitySummary = fetchActivitySummary(userId, year, month)
            activityYearly = fetchActivityYearly(userId, year)
            activityTotal = fetchActivityTotal(userId)
            recentActivities = fetchRecentActivities(userId, 5)
            dailyActivity = fetchDailyActivity(userId, year, month)
            monthlyActivity = fetchMonthlyActivity(userId, year)
            totalActivity = fetchTotalActivity(userId)
            isLoading = false
        }
    }

    // 하드코딩 데이터 삭제하고 API 데이터 변환
    val displayActivities = remember(recentActivities) {
        recentActivities?.map { activity ->
            RunningData(
                date = LocalDate.parse(activity.date),
                distance = activity.distance,
                duration = activity.duration,
                avgHeartRate = activity.avgHeartRate,
                calories = activity.calories
            )
        } ?: emptyList()  // API 데이터 없으면 빈 리스트
    }

    var selectedPeriod by remember { mutableStateOf(PeriodType.ALL) }

    val stats = remember(activitySummary, activityYearly, activityTotal, selectedPeriod) {
        when (selectedPeriod) {
            PeriodType.MONTH -> {
                if (activitySummary != null) {
                    RunningStats(
                        totalDistance = activitySummary!!.totalDistance,
                        runCount = activitySummary!!.runningCount,
                        avgPace = activitySummary!!.averagePace,
                        avgHeartRate = activitySummary!!.averageSteps
                    )
                } else {
                    RunningStats(0.0, 0, "0'00\"", 0)
                }
            }
            PeriodType.YEAR -> {
                if (activityYearly != null) {
                    RunningStats(
                        totalDistance = activityYearly!!.totalDistance,
                        runCount = activityYearly!!.runningCount,
                        avgPace = activityYearly!!.averagePace,
                        avgHeartRate = activityYearly!!.averageSteps
                    )
                } else {
                    RunningStats(0.0, 0, "0'00\"", 0)
                }
            }
            PeriodType.ALL -> {
                if (activityTotal != null) {
                    RunningStats(
                        totalDistance = activityTotal!!.totalDistance,
                        runCount = activityTotal!!.runningCount,
                        avgPace = activityTotal!!.averagePace,
                        avgHeartRate = activityTotal!!.averageSteps
                    )
                } else {
                    RunningStats(0.0, 0, "0'00\"", 0)
                }
            }
        }
    }

    val goalProgress = remember(stats) {
        val monthlyGoal = 100.0
        (stats.totalDistance / monthlyGoal).coerceIn(0.0, 1.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바 추가
        TopAppBar(
            title = { Text("활동") },
            navigationIcon = {
                IconButton(onClick = { /* 뒤로가기 동작 */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFC8E6C9),
                titleContentColor = Color(0xFF1B5E20)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 로딩 상태 표시
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF558B2F))
                }
            } else {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                StatsCard(stats = stats)
                Spacer(modifier = Modifier.height(16.dp))

                ActivityGraphSection(
                    selectedPeriod = selectedPeriod,
                    dailyData = dailyActivity?.activities,
                    monthlyData = monthlyActivity?.activities,
                    totalData = totalActivity?.activities
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 컨디션 레벨 카드 (클릭 가능)
                ConditionLevelCard(
                    conditionLevel = conditionLevel?.conditionLevel ?: 0,
                    onClick = onConditionClick
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "최근 활동",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // LazyColumn에서 일반 Column으로 변경
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayActivities.take(5).forEach { activity ->
                        ActivityItem(activity)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 목표 달성률 UI 개선
                GoalSection(
                    progress = goalProgress,
                    onClick = onGoalClick
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // API 데이터 상태 추가
    var monthlyAverage by remember { mutableStateOf<MonthlyAverageResponse?>(null) }
    var monthlyHeartZone by remember { mutableStateOf<MonthlyHeartZoneResponse?>(null) }
    var marathonFeedback by remember { mutableStateOf<MarathonFeedbackResponse?>(null) }
    var monthlySuggestions by remember { mutableStateOf<MonthlySuggestionsResponse?>(null) }
    var monthlyOverall by remember { mutableStateOf<MonthlyOverallResponse?>(null) }
    var performanceComparison by remember { mutableStateOf<PerformanceComparisonResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 현재 날짜
    val today = LocalDate.now()

    // API 호출
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userId = "user-1234"
            monthlyAverage = fetchMonthlyAverage(userId, today.year, today.monthValue)
            monthlyHeartZone = fetchMonthlyHeartZone(userId, today.year, today.monthValue)
            marathonFeedback = fetchMarathonFeedback(userId, today.year, today.monthValue)
            monthlySuggestions = fetchMonthlySuggestions(userId, today.year, today.monthValue)
            monthlyOverall = fetchMonthlyOverall(userId, today.year, today.monthValue)
            performanceComparison = fetchPerformanceComparison(userId, 7) // 추가
            isLoading = false
        }
    }

    // 사용자 목표 (실제로는 사용자 설정에서 가져와야 함)
    val userGoal = remember { UserGoal.MARATHON }

    // 동적 데이터들
    val goalProgress = 92 // 실제 계산값
    val weeklyDistance = monthlyAverage?.averageDistance ?: 0.0
    val weeklyRuns = monthlyAverage?.runningCount ?: 0
    val avgPace = monthlyAverage?.averagePace ?: "0'00\""
    val avgHeartRate = monthlyAverage?.averageHeartRate ?: 0

    // 페이스 유지력 분석 데이터
    val paceComparisonData = listOf(
        PaceComparisonData("10/09", 360, 380),
        PaceComparisonData("10/11", 360, 370),
        PaceComparisonData("10/12", 360, 390),
        PaceComparisonData("10/14", 360, 370),
        PaceComparisonData("10/16", 360, 380),
        PaceComparisonData("10/18", 360, 350)
    )

    val heartRateZones = remember(monthlyHeartZone) {
        monthlyHeartZone?.zones?.map { zone ->
            val color = when (zone.zoneName) {
                "회복 구간" -> Color(0xFF4CAF50)
                "유산소 구간" -> Color(0xFF2196F3)
                "템포 구간" -> Color(0xFFFF9800)
                "고강도 구간" -> Color(0xFFF44336)
                else -> Color(0xFF9E9E9E)
            }
            HeartRateZoneData(
                zoneName = zone.zoneName,
                range = "${zone.minBpm}-${zone.maxBpm} bpm",
                percentage = zone.percentage,
                color = color
            )
        } ?: emptyList()
    }

    // 목표별 마라톤 피드백
    val marathonFeedbackItems = remember(marathonFeedback) {
        marathonFeedback?.feedbacks?.map { feedback ->
            val (icon, title, color) = when (feedback.category) {
                "페이스 유지력" -> Triple("🎯", "페이스 유지력", Color(0xFF4CAF50))
                "심박수 관리" -> Triple("💙", "심박수 관리", Color(0xFF2196F3))
                "거리 달성" -> Triple("⚡", "거리 달성", Color(0xFFFF9800))
                else -> Triple("📊", feedback.category, Color(0xFF9E9E9E))
            }
            GoalFeedbackItem(
                icon = icon,
                title = title,
                score = feedback.score,
                description = feedback.feedback,
                color = color
            )
        } ?: emptyList()
    }

    // 향상 제안 (API에서 가져오기)
    val improvementSuggestions = remember(monthlySuggestions) {
        monthlySuggestions?.suggestions ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바
        TopAppBar(
            title = { Text("목표 달성 분석") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFC8E6C9),
                titleContentColor = Color(0xFF1B5E20)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 로딩 표시 추가
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF558B2F))
                }
            } else {
                // 목표 달성률
                GoalProgressCard(progress = goalProgress)

                Spacer(modifier = Modifier.height(16.dp))

                // 이번주 기록
                WeeklyRecordCard(
                    distance = weeklyDistance,
                    runs = weeklyRuns,
                    avgPace = avgPace,
                    avgHeartRate = avgHeartRate
                )

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // 페이스 유지력 분석
                PaceMaintenanceCard(data = paceComparisonData)

                Spacer(modifier = Modifier.height(16.dp))

                // 심박수 구간 분석
                HeartRateZoneCard(zones = heartRateZones)

                Spacer(modifier = Modifier.height(16.dp))

                // 목표 대비 실제 퍼포먼스
                PerformanceRadarCard(performanceData = performanceComparison)

                Spacer(modifier = Modifier.height(16.dp))

                // 마라톤 피드백
                MarathonFeedbackCard(userGoal = userGoal, feedbackItems = marathonFeedbackItems)
                Spacer(modifier = Modifier.height(16.dp))

                // 향상 제안
                ImprovementSuggestionsCard(suggestions = improvementSuggestions)

                Spacer(modifier = Modifier.height(16.dp))

                // 전체 평가
                OverallEvaluationCard(
                    evaluationText = monthlyOverall?.overallEvaluation ?: "평가 정보를 불러오는 중입니다.",
                )
            }
        }
    }
}


@Composable
fun GoalProgressCard(progress: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "목표 달성률",
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$progress%",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFC8E6C9),
                trackColor = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun WeeklyRecordCard(
    distance: Double,
    runs: Int,
    avgPace: String,
    avgHeartRate: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "월간 평균 통계",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(label = "거리", value = "${distance}km")
                MetricItem(label = "횟수", value = "${runs}회")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(label = "평균 페이스", value = avgPace)
                MetricItem(label = "평균 심박수", value = "${avgHeartRate}bpm")
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF558B2F)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
    }
}

@Composable
fun PaceMaintenanceCard(data: List<PaceComparisonData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "페이스 유지력 분석",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "목표 페이스 vs 실제 페이스 (초 단위)",
                fontSize = 13.sp,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "데이터 없음",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFFFFB74D), text = "목표 페이스")
                    Spacer(modifier = Modifier.width(12.dp))
                    LegendItem(color = Color(0xFF42A5F5), text = "실제 페이스")
                }

                Spacer(modifier = Modifier.height(8.dp))

                PaceComparisonChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

// 범례 아이템을 그리는 간단한 컴포저블
@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun HeartRateZoneCard(zones: List<HeartRateZoneData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "심박수 구간 분석",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            zones.forEach { zone ->
                HeartRateZoneItem(zone = zone)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PerformanceRadarCard(performanceData: PerformanceComparisonResponse?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "목표 대비 실제 퍼포먼스",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (performanceData == null || performanceData.categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "데이터 없음",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            } else {
                // 범례
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFFFFE0B2), text = "목표")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(color = Color(0xFF42A5F5), text = "실제")
                }

                Spacer(modifier = Modifier.height(16.dp))

                RadarChart(
                    data = performanceData.categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}

@Composable
fun RadarChart(data: List<PerformanceCategory>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f - 40f
        val angleStep = 360f / data.size

        // 배경 그리드 (동심원)
        for (i in 1..5) {
            val gridRadius = radius * i / 5f
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = gridRadius,
                center = center,
                style = Stroke(width = 1f)
            )
        }

        // 축 그리기
        data.forEachIndexed { index, category ->
            val angle = Math.toRadians((angleStep * index - 90).toDouble())
            val endPoint = Offset(
                center.x + (radius * cos(angle)).toFloat(),
                center.y + (radius * sin(angle)).toFloat()
            )
            drawLine(
                color = Color(0xFFE0E0E0),
                start = center,
                end = endPoint,
                strokeWidth = 1f
            )

            // 카테고리 레이블
            val labelDistance = radius + 30f
            val labelX = center.x + (labelDistance * cos(angle)).toFloat()
            val labelY = center.y + (labelDistance * sin(angle)).toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                category.name,
                labelX,
                labelY,
                Paint().apply {
                    color = Color(0xFF424242).toArgb()
                    textSize = 32f
                    textAlign = Paint.Align.CENTER
                }
            )
        }

        // 목표 오각형 (베이지색)
        val targetPath = Path().apply {
            data.forEachIndexed { index, category ->
                val angle = Math.toRadians((angleStep * index - 90).toDouble())
                val distance = radius * (category.target / 100f)
                val point = Offset(
                    center.x + (distance * cos(angle)).toFloat(),
                    center.y + (distance * sin(angle)).toFloat()
                )
                if (index == 0) moveTo(point.x, point.y)
                else lineTo(point.x, point.y)
            }
            close()
        }

        drawPath(
            path = targetPath,
            color = Color(0xFFFFE0B2).copy(alpha = 0.5f)
        )
        drawPath(
            path = targetPath,
            color = Color(0xFFFFB74D),
            style = Stroke(width = 3f)
        )

        // 실제 오각형 (파란색)
        val actualPath = Path().apply {
            data.forEachIndexed { index, category ->
                val angle = Math.toRadians((angleStep * index - 90).toDouble())
                val distance = radius * (category.actual / 100f)
                val point = Offset(
                    center.x + (distance * cos(angle)).toFloat(),
                    center.y + (distance * sin(angle)).toFloat()
                )
                if (index == 0) moveTo(point.x, point.y)
                else lineTo(point.x, point.y)
            }
            close()
        }

        drawPath(
            path = actualPath,
            color = Color(0xFF42A5F5).copy(alpha = 0.5f)
        )
        drawPath(
            path = actualPath,
            color = Color(0xFF42A5F5),
            style = Stroke(width = 3f)
        )

        // 데이터 포인트 표시
        data.forEachIndexed { index, category ->
            val angle = Math.toRadians((angleStep * index - 90).toDouble())

            // 실제 값 포인트
            val actualDistance = radius * (category.actual / 100f)
            val actualPoint = Offset(
                center.x + (actualDistance * cos(angle)).toFloat(),
                center.y + (actualDistance * sin(angle)).toFloat()
            )
            drawCircle(
                color = Color(0xFF42A5F5),
                radius = 6f,
                center = actualPoint
            )
        }
    }
}

@Composable
fun HeartRateZoneItem(zone: HeartRateZoneData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = zone.zoneName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = zone.range,
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
                Text(
                    text = "${zone.percentage}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = zone.color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = zone.percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = zone.color,
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun MarathonFeedbackCard(userGoal: UserGoal, feedbackItems: List<GoalFeedbackItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "${userGoal.displayName} 피드백",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            feedbackItems.forEach { item ->
                GoalFeedbackItemCard(item = item)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun GoalFeedbackItemCard(item: GoalFeedbackItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.icon,
                    fontSize = 32.sp
                )
                Column {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                text = item.score.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = item.color
            )
        }
    }
}

@Composable
fun ImprovementSuggestionsCard(suggestions: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "향상 제안",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(12.dp))

            suggestions.forEach { suggestion ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = suggestion,
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PaceComparisonChart(data: List<PaceComparisonData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 120f
        val chartHeight = size.height - 80f
        val maxValue = 400f
        // val minValue = 0f // 사용하지 않으므로 생략 가능
        val spacing = chartWidth / data.size
        val barWidth = spacing / 3f

        // 1. Y축 그리드 및 레이블
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = (i * 100).toString()

            // 그리드 라인
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(80f, y),
                end = Offset(chartWidth + 80f, y),
                strokeWidth = 1f
            )

            // Y축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                value,
                50f,
                y + 10f,
                Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 30f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        // 2. 막대 그래프 그리기
        data.forEachIndexed { index, paceData ->
            val centerX = 80f + spacing * index + spacing / 2f

            // 목표 페이스 (주황색)
            val targetHeight = (paceData.targetPace / maxValue) * chartHeight
            val targetY = chartHeight - targetHeight + 20f
            drawRect(
                color = Color(0xFFFFB74D),
                topLeft = Offset(centerX - barWidth - 2f, targetY),
                size = Size(barWidth, targetHeight)
            )

            // 실제 페이스 (파란색)
            val actualHeight = (paceData.actualPace / maxValue) * chartHeight
            val actualY = chartHeight - actualHeight + 20f
            drawRect(
                color = Color(0xFF42A5F5),
                topLeft = Offset(centerX + 2f, actualY),
                size = androidx.compose.ui.geometry.Size(barWidth, actualHeight)
            )

            // X축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                paceData.date,
                centerX,
                chartHeight + 50f,
                Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 26f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun OverallEvaluationCard(evaluationText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8CC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "전체 평가",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = evaluationText,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodButton("전체", selectedPeriod == PeriodType.ALL) { onPeriodSelected(PeriodType.ALL) }
        PeriodButton("년", selectedPeriod == PeriodType.YEAR) { onPeriodSelected(PeriodType.YEAR) }
        PeriodButton("월", selectedPeriod == PeriodType.MONTH) { onPeriodSelected(PeriodType.MONTH) }
    }
}

@Composable
fun RowScope.PeriodButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF558B2F) else Color(0xFFC8E6C9),
            contentColor = if (isSelected) Color.White else Color(0xFF2E7D32)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}

@Composable
fun StatsCard(stats: RunningStats) {
    val today = remember { LocalDate.now() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREAN) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 오늘 날짜 추가
            Text(
                text = today.format(dateFormatter),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = String.format("%.1f", stats.totalDistance),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(text = "킬로미터", fontSize = 14.sp, color = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "러닝", value = "${stats.runCount}회")
                StatItem(label = "평균 페이스", value = stats.avgPace)
                StatItem(label = "평균 심박수", value = "${stats.avgHeartRate}bpm")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF558B2F))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
    }
}

@Composable
fun ActivityItem(activity: RunningData) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.KOREAN)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = activity.date.format(formatter),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = activity.date.format(dayFormatter),
                    fontSize = 12.sp,
                    color = Color(0xFF558B2F)
                )
            }
            Text(
                text = "${activity.calories}kcal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = "${activity.distance}km",
                fontSize = 14.sp,
                color = Color(0xFF558B2F)
            )
        }
    }
}

@Composable
fun GoalSection(progress: Double, onClick: () -> Unit) {
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick), // 클릭 가능하게
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "목표 달성률", fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$percentage%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "이달의 목표: 100km", fontSize = 12.sp, color = Color(0xFFE8F5E9))
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFC8E6C9),
                trackColor = Color(0xFF2E7D32)
            )
        }
    }
}