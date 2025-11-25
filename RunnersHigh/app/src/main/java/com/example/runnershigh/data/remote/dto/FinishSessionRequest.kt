package com.example.runnershigh.data.remote.dto

/**
 * 러닝 세션 종료 API 요청 바디
 *
 * PATCH /api/sessions/{session_uuid}
 * 명세서에는 필드 구조가 없어서, 일반적인 러닝 앱에서 쓰는 값 기준으로 설계.
 * 실제 필드 이름/단위는 백엔드와 한 번 더 맞춰야 함!
 */
data class FinishSessionRequest(
    // 총 거리 (미터 단위)
    val distance_m: Int,

    // 총 러닝 시간 (초 단위)
    val duration_sec: Int,

    // 평균 페이스 (1km 당 몇 초인지, 예: 5분01초 = 301)
    val avg_pace_sec_per_km: Int,

    // 소모 칼로리 (kcal)
    val calories_kcal: Int,

    // 평균 심박수 (BPM)
    val avg_heart_rate_bpm: Int,

    // 총 상승 고도 (meter)
    val elevation_gain_m: Int,

    // 평균 케이던스 (steps per minute)
    val avg_cadence_spm: Int
)
