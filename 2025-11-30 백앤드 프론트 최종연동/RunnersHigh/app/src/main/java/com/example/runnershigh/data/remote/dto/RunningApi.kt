package com.example.runnershigh.data.remote.dto

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST

/**
 * 러닝 관련 API 모음
 *
 * 명세서 기준:
 *  - 러닝 세션 시작: POST /sessions/start
 *  - 러닝 세션 종료: PATCH /api/sessions/{session_uuid}
 *  - 러닝 결과 조회: GET /sessions/{sessionId}/result
 */
interface RunningApi {

    // 1) 러닝 세션 시작
    @POST("sessions/start")
    suspend fun startSession(
        @Body body: StartSessionRequest
    ): StartSessionResponse

    @POST("sessions/{sessionId}/gps")
    suspend fun uploadGpsPoint(
        @Path("sessionId") sessionId: String,
        @Body body: GpsPointRequest
    ): GpsPointResponse
    // 또는 BasicResponse<GpsPointResponse> 를 쓰는 구조라면 그 타입으로 변경

    //러닝 비교.
    @GET("sessions/{sessionId}/compare")
    suspend fun getRunningComparison(
        @Path("sessionId") sessionId: String
    ): RunningCompareResponse

  ///3.러닝종료.
  @POST("/sessions/{sessionId}/finish")
  suspend fun finishSession(
      @Path("sessionId") sessionId: String,
      @Body body: FinishSessionRequest
  ): FinishSessionResponse


    // 4.러닝 결과 조회
    @GET("/sessions/{sessionId}/result")
    suspend fun getSessionResult(
        @Path("sessionId") sessionId: String
    ): SessionResultResponse


}
