package com.pack.info_2.api // ⬅️ 이 패키지 경로는 MainActivity에서 import할 때 사용해야 합니다.

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.pack.info_2.UserService
object RetrofitClient {

    // ⚠️ 경고: 실제 서버의 기본 URL로 변경해야 합니다.
    private const val BASE_URL = "https://api.your-app-domain.com/"

    // Retrofit 인스턴스를 지연 초기화(Lazy Initialization)합니다.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // JSON 응답을 Kotlin 객체(UserLevel)로 변환하기 위해 Gson 컨버터를 사용합니다.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // UserService 인터페이스의 구현체(API 클라이언트)를 제공합니다.
    // 이 클라이언트를 통해 정의된 API 메서드를 호출할 수 있습니다.
    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
}