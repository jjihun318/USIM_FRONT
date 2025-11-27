package com.pack.info_2

import com.google.gson.annotations.SerializedName

data class UserLevel(
// 서버가 'user_id'를 사용하고 Kotlin에서 'userId'를 사용하고 싶을 경우
    @SerializedName("user_id")
    val userId: Int,

    // 서버가 'level'을 사용하고 Kotlin에서도 'level'을 사용하고 싶을 경우 (어노테이션 생략 가능)
    val level: Int,

    // 서버가 'exp_points'를 사용하고 Kotlin에서 'experiencePoints'를 사용하고 싶을 경우
    @SerializedName("exp_points")
    val experiencePoints: Int
)