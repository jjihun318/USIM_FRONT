/*package com.pack.myapplication.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Direction5Service {
    @GET("map-direction/v1/driving")
    suspend fun getRoute(
        // @Header가 아닌 @Query로 변경! (가장 중요)
        @Query("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Query("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("option") option: String = "trafast"
    ): Response<Direction5Response>
}

data class Direction5Response(
    val code: Int,
    val message: String,
    val currentDateTime: String,
    val route: Route?
)

data class Route(
    val trafast: List<Path>?
)

data class Path(
    val summary: Summary,
    val path: List<List<Double>>
)

data class Summary(
    val start: Location,
    val goal: Location,
    val distance: Int,
    val duration: Int,
    val etaServiceType: Int,
    val tollFare: Int,
    val taxiFare: Int,
    val fuelPrice: Int
)

data class Location(
    val location: List<Double>
)
*/