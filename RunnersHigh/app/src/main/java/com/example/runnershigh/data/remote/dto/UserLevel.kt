package com.example.runnershigh.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserLevel(

    @SerializedName("user_id")
    val userId: Int,

    val level: Int,

    @SerializedName("exp_points")
    val experiencePoints: Int
)