package com.pack.info_2

import com.google.gson.annotations.SerializedName

data class Mission(
    @SerializedName("mission_name")
    val missionName: String,

    @SerializedName("mission_description")
    val missionDescription: String,

    @SerializedName("progress_status")
    val progressStatus: String,

    @SerializedName("exp_points")
    val expPoints: Int,

    @SerializedName("mission_category")
    val missionCategory: String,

    @SerializedName("gauge_ratio")
    val gaugeRatio: Int  // 0~100 사이의 퍼센트 값
)

data class MissionListResponse(
    val missions: List<Mission>
)