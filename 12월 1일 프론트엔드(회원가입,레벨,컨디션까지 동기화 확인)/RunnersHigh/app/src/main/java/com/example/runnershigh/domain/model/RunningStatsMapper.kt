package com.example.runnershigh.domain.model

import com.example.runnershigh.data.remote.dto.*
// parsePaceToSeconds 를 만든 패키지에 맞게 import 해줘
import com.example.runnershigh.util.parsePaceToSeconds

/**
 * 서버에서 받은 세션 결과 → 앱에서 쓰는 RunningStats 로 변환하는 확장 함수
 */
/**
 * Pace 문자열("6'45\"") → 초(Int) 변환
 */
// SessionResultResponse -> RunningStats로 변환
fun SessionResultResponse.toRunningStats(): RunningStats {
    return RunningStats(
        distanceKm = distance,
        // "20:15" 같은 문자열을 초 단위로 변환
        durationSec = parseDurationToSeconds(duration),
        paceSecPerKm = parsePaceToSeconds(averagePace),
        calories = calories,
        // 서버에서 심박을 아직 안 주니까 0으로 채워둠 (나중에 필드 생기면 교체)
        avgHeartRate = 0,
        elevationGainM = elevationGain,
        cadence = cadence
    )
}

// "MM:SS" 형식 문자열을 초 단위로 변환하는 헬퍼
private fun parseDurationToSeconds(text: String): Int {
    // 예: "20:15" -> 20분 15초
    val parts = text.split(":")
    if (parts.size != 2) return 0
    val min = parts[0].toIntOrNull() ?: 0
    val sec = parts[1].toIntOrNull() ?: 0
    return min * 60 + sec
}
