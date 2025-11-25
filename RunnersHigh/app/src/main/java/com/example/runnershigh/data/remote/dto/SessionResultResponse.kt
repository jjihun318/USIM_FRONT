package com.example.runnershigh.data.remote.dto

/**
 * 러닝 세션 결과 조회 응답
 *
 * GET /sessions/{sessionId}/result
 * 화면에 보여줄 통계 값들이 들어온다고 가정.
 * 일단 FinishSessionRequest 와 거의 동일한 구조로 만들어 두고,
 * 나중에 백엔드에서 어떤 필드를 내려주는지 보고 수정하면 됨.
 */
data class SessionResultResponse(
    val distance_m: Int,
    val duration_sec: Int,
    val avg_pace_sec_per_km: Int,
    val calories_kcal: Int,
    val avg_heart_rate_bpm: Int,
    val elevation_gain_m: Int,
    val avg_cadence_spm: Int
    // TODO: 목표 대비, 레벨 보상, 배지 정보 등 추가되면 필드 확장
)