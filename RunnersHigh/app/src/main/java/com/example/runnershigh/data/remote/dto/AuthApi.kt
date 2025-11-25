package com.example.runnershigh.data.remote.dto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.PUT


interface AuthApi {

    // BASE_URL = "http://10.0.2.2:5001/runners-high-capstone/us-central1/"
    // 실제 엔드포인트는 signup_api
    @POST("signup_api")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>
    //이메일 체크
    @GET("auth/check-email")
    suspend fun checkEmail(
        @Query("email") email: String
    ): Response<EmailCheckResponse>

    // ✅ username(닉네임) 중복 체크
    @GET("auth/check-username")
    suspend fun checkUsername(
        @Query("username") username: String
    ): Response<UsernameCheckResponse>
    // 로그인

    @POST("login_api")           // 또는 "users/login" 등, 백엔드와 맞추기
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // 👇 신장·체중 업데이트
    @PUT("update_body_api")
    suspend fun updateBody(
        @Body request: BodyUpdateRequest
    ): Response<BodyUpdateResponse>

}
