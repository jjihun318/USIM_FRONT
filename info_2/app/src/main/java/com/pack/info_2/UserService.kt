package com.pack.info_2

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

    // ✅ 수정: 전체 배지 목록 조회 (진행 중인 배지용)
    @GET("badges")
    suspend fun getAllBadges(): Response<List<Badge>>

    // ✅ 수정: 사용자가 획득한 배지 조회
    @GET("users/{userId}/badges/acquired")
    suspend fun getAcquiredBadges(
        @Path("userId") userId: Int
    ): Response<List<AcquiredBadge>>

    @GET("users/{userId}/condition")
    suspend fun getUserCondition(
        @Path("userId") userId: Int
    ): Response<UserCondition>
}