package com.pack.info_2

import com.google.gson.annotations.SerializedName

// 배지 리스트 데이터 클래스 (badge_1 ~ badge_5용)
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

// 획득한 배지 데이터 클래스 (badge_6 ~ badge_8용)
data class AcquiredBadge(
    @SerializedName("mission_name")
    val missionName: String,

    @SerializedName("mission_description")
    val missionDescription: String,

    @SerializedName("acquired_date")
    val acquiredDate: String
)