package com.example.runnershigh.data.remote.dto

import com.example.runnershigh.data.remote.ApiEndpoints
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
//ApiEndpoints.ACQUIRE_BADGE_API
    @GET(ApiEndpoints.GET_USER_LEVEL_API)
    suspend fun getUserLevel(
        @Path("userId") userId: Int
    ): Response<UserLevel>

    @GET("missions")
    suspend fun getUserMissions(
        @Query("userId") userId: Int
    ): Response<List<Mission>>

    @GET(ApiEndpoints.GET_BADGE_LIST_API)
    suspend fun getAllBadges(): Response<List<Badge>>

    @GET(ApiEndpoints.ACQUIRE_BADGE_API)
    suspend fun getAcquiredBadges(
        @Path("userId") userId: Int
    ): Response<List<AcquiredBadge>>

    @GET(ApiEndpoints.GET_CONDITION_DETAIL_API)
    suspend fun getUserCondition(
        @Path("userId") userId: Int
    ): Response<UserCondition>
}