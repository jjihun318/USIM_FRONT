package com.example.runnershigh.data.remote

import com.example.runnershigh.data.remote.dto.AuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Retrofit + OkHttp 클라이언트 설정
 *
 * BASE_URL 은 현재 Cloud Functions / 백엔드 주소에 맞게 수정해줘야 함.
 */
object ApiClient {

    // Cloud Functions 예시:
    // http://127.0.0.1:5001/runners-high-capstone/us-central1/...
    //
    // ⚠ 에뮬레이터에서 PC 로컬 서버에 붙을 땐 127.0.0.1 대신 10.0.2.2 를 써야 함.
    private const val BASE_URL =
        "http://10.0.2.2:5001/runners-high-capstone/us-central1/"

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            // BODY 로 하면 요청/응답 JSON 전부 로그로 볼 수 있어서 개발 초기에 편함
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    // 러닝 세션 관련 API
    val runningApi: RunningApi by lazy {
        retrofit.create(RunningApi::class.java)
    }

    // ✅ 회원가입/로그인 등 Auth 관련 API
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}
