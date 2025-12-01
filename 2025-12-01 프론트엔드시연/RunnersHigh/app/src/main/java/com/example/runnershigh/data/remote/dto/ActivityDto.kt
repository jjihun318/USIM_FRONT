package com.example.runnershigh.data.remote.dto

data class ActivitySummaryResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val totalDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageSteps: Int
)

data class ActivityYearlyResponse(
    val userId: String,
    val year: Int,
    val totalDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageSteps: Int
)

data class ActivityTotalResponse(
    val userId: String,
    val totalDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageSteps: Int,
    val startDate: String
)

data class RecentActivitiesResponse(
    val activities: List<RecentActivityResponse>
)

data class RecentActivityResponse(
    val activityId: String,
    val date: String,
    val distance: Double,
    val duration: Int,
    val avgHeartRate: Int,
    val calories: Int
)

data class MonthlyAverageResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val averageDistance: Double,
    val runningCount: Int,
    val averagePace: String,
    val averageHeartRate: Int
)

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

data class MonthlySuggestionsResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val suggestions: List<String>
)

data class MonthlyOverallResponse(
    val userId: String,
    val year: Int,
    val month: Int,
    val overallEvaluation: String
)

data class ConditionLevelResponse(
    val userId: String,
    val conditionLevel: Int,
    val analysis: String,
    val lastUpdated: String
)

data class AchievementResponse(
    val totalMissions: Int,
    val completedMissions: Int,
    val achievementRate: Int
)