package com.example.runnershigh.data.remote.dto

import com.squareup.moshi.Json


// 🔵 서버에서 내려오는 전체 배지 정보 DTO
data class Badge(
    @Json(name = "mission_name")
    val missionName: String,

    @Json(name = "mission_description")
    val missionDescription: String,

    @Json(name = "mission_detail")
    val missionDetail: String,

    @Json(name = "progress_status")
    val progressStatus: String,

    @Json(name = "gauge_ratio")
    val gaugeRatio: Int
)

// 🔵 서버에서 내려오는 획득 배지 DTO
data class AcquiredBadge(
    @Json(name = "mission_name")
    val missionName: String,

    @Json(name = "mission_description")
    val missionDescription: String,

    @Json(name = "acquired_date")
    val acquiredDate: String
)
