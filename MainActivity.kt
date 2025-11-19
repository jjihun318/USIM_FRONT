package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.HeartRateData
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectManager: HealthConnectManager

    private var stepCountData by mutableStateOf(0)
    private var dailyCaloriesBurnedData by mutableStateOf(0)
    private var distanceWalkedData by mutableStateOf(0)

    // ✅ 평균 심박수 상태
    private var avgHeartRateText by mutableStateOf("")

    // ✅ 수면 데이터 상태
    private var sleepDurationText by mutableStateOf("")
    private var deepSleepText by mutableStateOf("")
    private var lightSleepText by mutableStateOf("")
    private var remSleepText by mutableStateOf("")
    private var awakeSleepText by mutableStateOf("")

    private val permissionLauncher =
        registerForActivityResult<Set<String>, Set<String>>(
            PermissionController.createRequestPermissionResultContract()
        ) { granted: Set<String> ->
            Log.d("HEALTH_SYNC", "권한 요청 결과: $granted")
            if (granted.containsAll(healthConnectManager.permissions)) {
                fetchAndSend { stepData, dailyCaloriesBurned, distanceWalked ->
                    stepCountData = stepData
                    dailyCaloriesBurnedData = dailyCaloriesBurned
                    distanceWalkedData = distanceWalked
                }
            } else {
                Log.e("HEALTH_SYNC", "권한 요청 실패")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectManager = HealthConnectManager(this)

        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Runner's High data test",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Text(text = "걸음수 데이터: $stepCountData 보")
                Text(text = "총 칼로리 소모량: $dailyCaloriesBurnedData kcal")
                Text(text = "오늘 걸은 거리: $distanceWalkedData m")

                // ✅ 평균 심박수 표시
                Text(text = "평균 심박수: $avgHeartRateText BPM")

                // ✅ 수면 데이터 표시
                Text(
                    text = "수면 시간: $sleepDurationText",
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(text = "깊은 수면: $deepSleepText")
                Text(text = "얕은 수면: $lightSleepText")
                Text(text = "렘 수면: $remSleepText")
                Text(text = "깬 상태: $awakeSleepText")

                Button(
                    onClick = {
                        permissionLauncher.launch(healthConnectManager.permissions)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "오늘 데이터 가져오기 및 서버 전송")
                }
            }
        }
    }

    private fun fetchAndSend(onDataFetched: (Int, Int, Int) -> Unit) {
        lifecycleScope.launch {
            try {
                val stepData = healthConnectManager.getTodayStepCount()
                val dailyCaloriesBurned = healthConnectManager.getTodayCaloriesBurned()
                val totalDistance = healthConnectManager.getTodayDistanceWalked()

                val heartRateRecords = healthConnectManager.readHeartRates()
                val allSamples = heartRateRecords.flatMap { it.samples }

                // ✅ 평균 심박수 계산
                val avgHeartRate = if (allSamples.isNotEmpty()) {
                    allSamples.map { it.beatsPerMinute }.average()
                } else 0.0

                avgHeartRateText = avgHeartRate.toInt().toString()

                // ✅ 가장 최근 수면 데이터 가져오기
                val sleepDuration = healthConnectManager.getLatestSleepDuration()
                val sleepStageInfo = healthConnectManager.getLatestSleepStageInfo()

                // ✅ 수면 데이터 UI 업데이트
                sleepDurationText = if (sleepDuration > 0) {
                    "${sleepDuration / 60}시간 ${sleepDuration % 60}분 (${sleepStageInfo.sleepStartTime} ~ ${sleepStageInfo.sleepEndTime})"
                } else {
                    "데이터 없음"
                }
                deepSleepText = "${sleepStageInfo.deepSleepMinutes}분"
                lightSleepText = "${sleepStageInfo.lightSleepMinutes}분"
                remSleepText = "${sleepStageInfo.remSleepMinutes}분"
                awakeSleepText = "${sleepStageInfo.awakeSleepMinutes}분"

                onDataFetched(stepData, dailyCaloriesBurned, totalDistance)

                val heartRateData = allSamples.map {
                    HeartRateData(
                        bpm = it.beatsPerMinute.toDouble(),
                        time = it.time.toString()
                    )
                }

                // ✅ 수면 데이터를 서버 전송 데이터에 추가
                val healthData = HealthData(
                    stepData = listOf(stepData),
                    heartRateData = heartRateData,
                    caloriesBurnedData = dailyCaloriesBurned.toDouble(),
                    distanceWalked = totalDistance.toDouble(),
                    sleepDurationMinutes = sleepDuration,
                    deepSleepMinutes = sleepStageInfo.deepSleepMinutes,
                    lightSleepMinutes = sleepStageInfo.lightSleepMinutes,
                    remSleepMinutes = sleepStageInfo.remSleepMinutes,
                    awakeSleepMinutes = sleepStageInfo.awakeSleepMinutes
                )

                RetrofitClient.apiService.sendHealthData(healthData)
                Log.d("HEALTH_SYNC", "데이터 전송 완료 - 수면시간: ${sleepDuration}분 (${sleepStageInfo.sleepStartTime} ~ ${sleepStageInfo.sleepEndTime})")
            } catch (e: Exception) {
                Log.e("HEALTH_SYNC", "에러 발생", e)
            }
        }
    }
}

// ✅ 서버 전송용 데이터 클래스 (활동 칼로리 제거, 수면 데이터 포함)
data class HealthData(
    val stepData: List<Int>,
    val heartRateData: List<HeartRateData>,
    val caloriesBurnedData: Double,
    val distanceWalked: Double,
    val sleepDurationMinutes: Int,
    val deepSleepMinutes: Int,
    val lightSleepMinutes: Int,
    val remSleepMinutes: Int,
    val awakeSleepMinutes: Int
)