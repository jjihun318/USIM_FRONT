package com.example.runnershigh.data.remote.dto


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @GET("users/{userId}/level")
    suspend fun getUserLevel(
        @Path("userId") userId: Int
    ): Response<UserLevel>

    @GET("missions")
    suspend fun getUserMissions(
        @Query("userId") userId: Int
    ): Response<List<Mission>>

    @GET("badges")
    suspend fun getAllBadges(): Response<List<Badge>>

    @GET("users/{userId}/badges/acquired")
    suspend fun getAcquiredBadges(
        @Path("userId") userId: Int
    ): Response<List<AcquiredBadge>>

    @GET("users/{userId}/condition")
    suspend fun getUserCondition(
        @Path("userId") userId: Int
    ): Response<UserCondition>
}