package com.example.runnershigh.data.remote.dto

import com.example.runnershigh.data.remote.ApiEndpoints
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

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

    @POST(ApiEndpoints.START_RUNNING_API)
    suspend fun startSession(
        @Body body: StartSessionRequest
    ): StartSessionResponse

    @POST("${ApiEndpoints.START_RUNNING_API}/{sessionId}/gps")
    suspend fun uploadGpsPoint(
        @Path("sessionId") sessionId: String,
        @Body body: GpsPointRequest
    ): GpsPointResponse
    // 또는 BasicResponse<GpsPointResponse> 를 쓰는 구조라면 그 타입으로 변경

    //러닝 비교.
    @GET(ApiEndpoints.GET_SESSION_DETAIL_API)
    suspend fun getRunningComparison(
        @Query("sessionId") sessionId: String,
        @Query("distance") distanceMeters: Double,
        @Query("time") elapsedSeconds: Int
    ): RunningCompareResponse

  ///3.러닝종료.
  @PATCH(ApiEndpoints.COMPLETE_RUNNING_API)
  suspend fun finishSession(

      @Body body: FinishSessionRequest
  ): FinishSessionResponse


  // 4.러닝 결과 조회
    @GET(ApiEndpoints.GET_SESSION_DETAIL_API)
      suspend fun getSessionResult(
        @Query("sessionId") sessionId: String,
        @Query("userId") userId: String

    ): SessionResultResponse

    // 5.러닝 피드백 저장
    @POST(ApiEndpoints.CREATE_FEEDBACK_API)
    suspend fun submitFeedback(
        @Body body: RunningFeedbackRequest
    ): RunningFeedbackResponse

    // 이미 제출된 피드백 조회
    @GET(ApiEndpoints.GET_FEEDBACK_API)
    suspend fun getSubmittedFeedback(
        @Query("user_uuid") userUuid: String,
        @Query("sessionId") sessionId: String
    ): List<SubmittedFeedback>



}
