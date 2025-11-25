package com.example.runnershigh.data.remote

import com.example.runnershigh.data.remote.dto.FinishSessionRequest
import com.example.runnershigh.data.remote.dto.SessionResultResponse
import com.example.runnershigh.data.remote.dto.StartSessionResponse
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
    @POST("/sessions/start")
    suspend fun startSession(): StartSessionResponse

    // 2) 러닝 세션 종료
    @PATCH("/api/sessions/{sessionId}")
    suspend fun finishSession(
        @Path("sessionId") sessionId: String,
        @Body body: FinishSessionRequest
    )

    // 3) 러닝 결과 조회
    @GET("/sessions/{sessionId}/result")
    suspend fun getSessionResult(
        @Path("sessionId") sessionId: String
    ): SessionResultResponse
}
