package com.example.runnershigh.data.remote

import com.example.runnershigh.data.remote.dto.AuthApi
import com.example.runnershigh.data.remote.dto.RunningApi
import com.example.runnershigh.data.remote.dto.UserService
import com.example.runnershigh.data.remote.dto.HealthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Retrofit + OkHttp 클라이언트 설정
 */
object ApiClient {

    // 에뮬레이터 → 로컬 서버 접근용 host
    private const val BASE_URL =
        "http://10.0.2.2:5001/runners-high-capstone/us-central1/"

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // ✅ Kotlin data class 를 위해 Moshi 에 KotlinJsonAdapterFactory 추가
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val runningApi: RunningApi by lazy {
        retrofit.create(RunningApi::class.java)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    // ✅ 헬스 데이터 전송용 API
    val healthApi: HealthApi by lazy {
        retrofit.create(HealthApi::class.java)
    }
}
