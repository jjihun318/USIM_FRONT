package com.example.runnershigh.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.*

class HealthConnectManager(private val context: Context) {

    // lazy 로 만들어 두면 필요할 때만 생성됨
    val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    // 외부에서 ActivityResultLauncher 로 권한 요청할 때 쓸 permission set
    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
    )




    // ✅ 오늘 걸음 수 총합
    suspend fun getTodayStepCount(): Int {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayStart = today.atStartOfDay(zoneId).toInstant()
        val todayEnd = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

        val request = AggregateRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
        )
        val result = healthConnectClient.aggregate(request)
        return (result[StepsRecord.COUNT_TOTAL] as Long? ?: 0L).toInt()
    }

    // ✅ 오늘 소모 칼로리 (kcal)
    suspend fun getTodayCaloriesBurned(): Int {
        val zoneId = ZoneId.systemDefault()
        val todayStart = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()
        val now = Instant.now()

        val request = AggregateRequest(
            metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(todayStart, now)
        )
        val result = healthConnectClient.aggregate(request)

        val energy = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
        return energy?.inKilocalories?.toInt() ?: 0
    }

    // ✅ 오늘 걸은 거리 (미터)
    suspend fun getTodayDistanceWalked(): Int {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayStart = today.atStartOfDay(zoneId).toInstant()
        val todayEnd = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

        val request = AggregateRequest(
            metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
        )
        val result = healthConnectClient.aggregate(request)

        val distance = result[DistanceRecord.DISTANCE_TOTAL]
        return distance?.inMeters?.toInt() ?: 0
    }

    // ✅ 오늘 심박수 목록
    suspend fun readHeartRates(): List<HeartRateRecord> {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayStart = today.atStartOfDay(zoneId).toInstant()
        val todayEnd = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
        )
        return healthConnectClient.readRecords(request).records
    }

    // 지난 48시간 수면 세션
    private suspend fun readRecentSleepSessions(): List<SleepSessionRecord> {
        val zoneId = ZoneId.systemDefault()
        val twoDaysAgo = LocalDate.now(zoneId)
            .minusDays(2)
            .atStartOfDay(zoneId)
            .toInstant()
        val now = Instant.now()

        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(twoDaysAgo, now)
        )
        return healthConnectClient.readRecords(request).records
    }

    // ✅ 최근 수면 세션 총 수면 시간(분)
    suspend fun getLatestSleepDuration(): Int {
        val sessions = readRecentSleepSessions()
        if (sessions.isEmpty()) return 0

        val latestSession = sessions.maxByOrNull { it.endTime } ?: return 0
        return Duration.between(latestSession.startTime, latestSession.endTime)
            .toMinutes()
            .toInt()
    }

    // ✅ 수면 단계 요약 정보
    data class SleepStageInfo(
        val deepSleepMinutes: Int,
        val lightSleepMinutes: Int,
        val remSleepMinutes: Int,
        val awakeSleepMinutes: Int,
        val totalSleepMinutes: Int,
        val sleepStartTime: String,
        val sleepEndTime: String
    )

    suspend fun getLatestSleepStageInfo(): SleepStageInfo {
        val sessions = readRecentSleepSessions()
        if (sessions.isEmpty()) {
            return SleepStageInfo(0, 0, 0, 0, 0, "데이터 없음", "데이터 없음")
        }

        val latestSession = sessions.maxByOrNull { it.endTime }
            ?: return SleepStageInfo(0, 0, 0, 0, 0, "데이터 없음", "데이터 없음")

        var deepSleep = 0
        var lightSleep = 0
        var remSleep = 0
        var awakeSleep = 0

        latestSession.stages.forEach { stage ->
            val duration = Duration.between(stage.startTime, stage.endTime)
                .toMinutes()
                .toInt()
            when (stage.stage) {
                SleepSessionRecord.STAGE_TYPE_DEEP -> deepSleep += duration
                SleepSessionRecord.STAGE_TYPE_LIGHT -> lightSleep += duration
                SleepSessionRecord.STAGE_TYPE_REM -> remSleep += duration
                SleepSessionRecord.STAGE_TYPE_AWAKE -> awakeSleep += duration
                else -> Unit
            }
        }

        val total = deepSleep + lightSleep + remSleep + awakeSleep

        val zoneId = ZoneId.systemDefault()
        val startTime = latestSession.startTime.atZone(zoneId)
        val endTime = latestSession.endTime.atZone(zoneId)

        val startTimeStr =
            "${startTime.monthValue}월 ${startTime.dayOfMonth}일 ${
                String.format(
                    "%02d:%02d",
                    startTime.hour,
                    startTime.minute
                )
            }"
        val endTimeStr =
            "${endTime.monthValue}월 ${endTime.dayOfMonth}일 ${
                String.format(
                    "%02d:%02d",
                    endTime.hour,
                    endTime.minute
                )
            }"

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
