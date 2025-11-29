package com.example.runnershigh.data.remote.dto

import com.google.gson.annotations.SerializedName

// 🔵 서버에서 내려오는 전체 배지 정보 DTO
data class Badge(
    @SerializedName("mission_name")
    val missionName: String,

    @SerializedName("mission_description")
    val missionDescription: String,

    @SerializedName("mission_detail")
    val missionDetail: String,

    @SerializedName("progress_status")
    val progressStatus: String,

    @SerializedName("gauge_ratio")
    val gaugeRatio: Int
)

// 🔵 서버에서 내려오는 획득 배지 DTO
data class AcquiredBadge(
    @SerializedName("mission_name")
    val missionName: String,

    @SerializedName("mission_description")
    val missionDescription: String,

    @SerializedName("acquired_date")
    val acquiredDate: String
)
