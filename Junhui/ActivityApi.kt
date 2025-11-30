package com.example.runnershigh.data.remote.api

import com.example.runnershigh.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Query

// activity(활동 api 모음)
interface ActivityApi {

    @GET("activity_summary_api")
    suspend fun getActivitySummary(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): ActivitySummaryResponse

    @GET("activity_yearly_api")
    suspend fun getActivityYearly(
        @Query("userId") userId: String,
        @Query("year") year: Int
    ): ActivityYearlyResponse

    @GET("activity/summary/total")
    suspend fun getActivityTotal(
        @Query("userId") userId: String
    ): ActivityTotalResponse

    @GET("activity/recent")
    suspend fun getRecentActivities(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 5
    ): RecentActivitiesResponse

    @GET("activity/monthly_average")
    suspend fun getMonthlyAverage(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): MonthlyAverageResponse

    @GET("activity/monthly-heart-zone")
    suspend fun getMonthlyHeartZone(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): MonthlyHeartZoneResponse

    @GET("activity/marathon-feedback")
    suspend fun getMarathonFeedback(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): MarathonFeedbackResponse

    @GET("activity/monthly-suggestions")
    suspend fun getMonthlySuggestions(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): MonthlySuggestionsResponse

    @GET("activity/monthly-overall")
    suspend fun getMonthlyOverall(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): MonthlyOverallResponse

    @GET("activity/condition")
    suspend fun getConditionLevel(
        @Query("userId") userId: String
    ): ConditionLevelResponse

    @GET("activity/achievement_api")
    suspend fun getAchievement(
        @Query("userId") userId: String
    ): AchievementResponse
}