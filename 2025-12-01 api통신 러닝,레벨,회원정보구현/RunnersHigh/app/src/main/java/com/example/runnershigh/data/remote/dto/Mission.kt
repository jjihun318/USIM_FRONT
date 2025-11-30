package com.example.runnershigh.data.remote.dto

import com.squareup.moshi.Json

data class Mission(
    @Json(name = "mission_name")
    val missionName: String,

    @Json(name = "mission_description")
    val missionDescription: String,

    @Json(name = "mission_detail")
    val missionDetail: String? = null,

    @Json(name = "progress_status")
    val progressStatus: String,

    @Json(name = "exp_points")
    val expPoints: Int,

    @Json(name = "mission_category")
    val missionCategory: String? = null,

    @Json(name = "gauge_ratio")
    val gaugeRatio: Int   // 0~100 between percent
)

data class MissionListResponse(
    val missions: List<Mission>
)