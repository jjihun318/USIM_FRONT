package com.example.myapplication

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.Duration

class HealthConnectManager(private val context: Context) {

    val healthConnectClient = HealthConnectClient.getOrCreate(context)

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
    )

    // ✅ [Aggregate] 걸음수 총합 (Int 반환)
    suspend fun getTodayStepCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val todayEnd = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()

        val request = AggregateRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
        )
        val result = healthConnectClient.aggregate(request)
        return (result[StepsRecord.COUNT_TOTAL] as Long? ?: 0L).toInt()
    }

    // ✅ [Aggregate] 총 소모 칼로리 (Int 반환) - 00:00부터 23:59까지
    suspend fun getTodayCaloriesBurned(): Int {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()

        // 오늘 자정 (00:00:00)
        val todayStart = LocalDate.now(zoneId)
            .atStartOfDay(zoneId)
            .toInstant()

        // 현재 시간 (또는 오늘 끝: 23:59:59)
        val todayEnd = now

        val request = AggregateRequest(
            metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
        )
        val result = healthConnectClient.aggregate(request)

        val energy = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
        return energy?.inKilocalories?.toInt() ?: 0
    }

    // ✅ [Aggregate] 걸은 거리 (Int 반환, 미터 기준)
    suspend fun getTodayDistanceWalked(): Int {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val todayEnd = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()

        val request = AggregateRequest(
            metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
        )
        val result = healthConnectClient.aggregate(request)

        // Distance 객체에서 inMeters 값을 추출
        val distance = result[DistanceRecord.DISTANCE_TOTAL]
        return distance?.inMeters?.toInt() ?: 0
    }

    // [ReadRecords] 심박수 (List 반환)
    suspend fun readHeartRates(): List<HeartRateRecord> {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val todayEnd = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()

        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
        )
        return healthConnectClient.readRecords(request).records
    }

    // ✅ [ReadRecords] 최근 수면 세션 (지난 48시간 내)
    private suspend fun readRecentSleepSessions(): List<SleepSessionRecord> {
        // 지난 48시간 동안의 수면 세션 조회 (넉넉하게)
        val twoDaysAgo = LocalDate.now().minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val now = Instant.now()

        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(twoDaysAgo, now)
        )
        return healthConnectClient.readRecords(request).records
    }

    // ✅ [분석] 가장 최근 수면 시간 (분 단위 반환)
    // 가장 최근에 종료된 수면 세션 1개만 반환
    suspend fun getLatestSleepDuration(): Int {
        val sessions = readRecentSleepSessions()
        if (sessions.isEmpty()) return 0

        // 종료 시간 기준으로 가장 최근 수면 세션 선택
        val latestSession = sessions.maxByOrNull { it.endTime }
        return latestSession?.let {
            Duration.between(it.startTime, it.endTime).toMinutes().toInt()
        } ?: 0
    }

    // ✅ [분석] 가장 최근 수면 세션의 단계별 정보
    data class SleepStageInfo(
        val deepSleepMinutes: Int,
        val lightSleepMinutes: Int,
        val remSleepMinutes: Int,
        val awakeSleepMinutes: Int,
        val totalSleepMinutes: Int,
        val sleepStartTime: String,  // 수면 시작 시간
        val sleepEndTime: String      // 수면 종료 시간
    )

    suspend fun getLatestSleepStageInfo(): SleepStageInfo {
        val sessions = readRecentSleepSessions()

        // 데이터가 없으면 빈 정보 반환
        if (sessions.isEmpty()) {
            return SleepStageInfo(0, 0, 0, 0, 0, "데이터 없음", "데이터 없음")
        }

        // 가장 최근에 종료된 수면 세션만 선택
        val latestSession = sessions.maxByOrNull { it.endTime } ?: return SleepStageInfo(0, 0, 0, 0, 0, "데이터 없음", "데이터 없음")

        var deepSleep = 0
        var lightSleep = 0
        var remSleep = 0
        var awakeSleep = 0

        // 해당 세션의 단계별 시간 계산
        latestSession.stages.forEach { stage ->
            val duration = Duration.between(stage.startTime, stage.endTime).toMinutes().toInt()
            when (stage.stage) {
                SleepSessionRecord.STAGE_TYPE_DEEP -> deepSleep += duration
                SleepSessionRecord.STAGE_TYPE_LIGHT -> lightSleep += duration
                SleepSessionRecord.STAGE_TYPE_REM -> remSleep += duration
                SleepSessionRecord.STAGE_TYPE_AWAKE -> awakeSleep += duration
                else -> {} // 기타 단계는 무시
            }
        }

        val total = deepSleep + lightSleep + remSleep + awakeSleep

        // 수면 시작/종료 시간 포맷팅
        val zoneId = ZoneId.systemDefault()
        val startTime = latestSession.startTime.atZone(zoneId)
        val endTime = latestSession.endTime.atZone(zoneId)

        val startTimeStr = "${startTime.monthValue}월 ${startTime.dayOfMonth}일 ${String.format("%02d:%02d", startTime.hour, startTime.minute)}"
        val endTimeStr = "${endTime.monthValue}월 ${endTime.dayOfMonth}일 ${String.format("%02d:%02d", endTime.hour, endTime.minute)}"

        return SleepStageInfo(
            deepSleepMinutes = deepSleep,
            lightSleepMinutes = lightSleep,
            remSleepMinutes = remSleep,
            awakeSleepMinutes = awakeSleep,
            totalSleepMinutes = total,
            sleepStartTime = startTimeStr,
            sleepEndTime = endTimeStr
        )
    }
}