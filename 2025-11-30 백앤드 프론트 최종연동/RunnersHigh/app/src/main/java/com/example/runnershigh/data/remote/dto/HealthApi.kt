package com.example.runnershigh.data.remote.dto

import com.example.runnershigh.data.remote.dto.HealthData
import retrofit2.http.Body
import retrofit2.http.POST


interface HealthApi {

    @POST("/hh/receive")
    suspend fun sendHealthData(
        @Body healthData: HealthData
    )
}