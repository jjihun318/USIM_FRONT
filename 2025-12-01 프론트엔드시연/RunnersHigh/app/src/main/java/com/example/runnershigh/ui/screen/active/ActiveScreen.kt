package com.example.runnershigh.ui.screen.active

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.runnershigh.data.health.HealthConnectManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ====== 공용 모델 ======

enum class PeriodType { ALL, YEAR, MONTH }

data class RunningStats(
    val totalDistanceKm: Double,
    val runCount: Int,
    val avgPace: String,
    val avgHeartRate: Int
)

data class DailyActivityData(
    val day: Int,
    val distanceKm: Double
)

data class MonthlyActivityData(
    val month: Int,
    val distanceKm: Double
)

data class TotalActivityData(
    val year: Int,
    val month: Int,
    val distanceKm: Double
)

// 날짜를 LocalDate 대신 문자열로만 보관
data class RunningData(
    val dateLabel: String,   // 예: "2025-11-29 (토)"
    val distanceKm: Double,
    val calories: Int
)

// ====== 메인 화면 ======

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val healthManager = remember { HealthConnectManager(context) }

    var stats by remember {
        mutableStateOf(
            RunningStats(
                totalDistanceKm = 0.0,
                runCount = 0,
                avgPace = "0'00\"",
                avgHeartRate = 0
            )
        )
    }

    // 그래프용 더미 데이터 (나중에 서버/DB 값으로 교체하면 됨)
    val dailyData = remember {
        (1..10).map { day ->
            DailyActivityData(day = day, distanceKm = 3.0 + day * 0.2)
        }
    }
    val monthlyData = remember {
        (1..12).map { m ->
            MonthlyActivityData(month = m, distanceKm = 40.0 + m * 3)
        }
    }
    val totalData = remember {
        listOf(
            TotalActivityData(2024, 11, 280.0),
            TotalActivityData(2025, 1, 320.0)
        )
    }

    // 최근 활동 더미 (날짜는 그냥 문자열)
    val recentActivities = remember {
        listOf(
            RunningData("2025-11-29 (토)", 6.0, 380),
            RunningData("2025-11-28 (금)", 5.5, 360),
            RunningData("2025-11-27 (목)", 5.0, 340),
            RunningData("2025-11-26 (수)", 4.8, 330),
            RunningData("2025-11-25 (화)", 4.5, 320)
        )
    }

    var selectedPeriod by remember { mutableStateOf(PeriodType.ALL) }
    var isLoading by remember { mutableStateOf(true) }

    // HealthConnect에서 오늘 기록 간단히 읽어와서 통계에 반영
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val distanceM = healthManager.getTodayDistanceWalked()
            val distanceKm = distanceM / 1000.0
            val calories = healthManager.getTodayCaloriesBurned()
            val steps = healthManager.getTodayStepCount()

            stats = stats.copy(
                totalDistanceKm = distanceKm,
                runCount = if (distanceKm > 0.0) 1 else 0,
                avgPace = "6'00\"",            // 실제 페이스는 나중에 Running API로 계산
                avgHeartRate = 120 + (steps / 1000)
            )
        } catch (e: Exception) {
            // 아직 권한 없거나 데이터 없으면 0으로 둔다.
        } finally {
            isLoading = false
        }
    }

    val monthlyGoalKm = 100.0
    val goalProgress = (stats.totalDistanceKm / monthlyGoalKm).coerceIn(0.0, 1.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text("활동") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFC8E6C9),
                titleContentColor = Color(0xFF1B5E20)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF558B2F))
                }
            } else {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                StatsCard(stats = stats)
                Spacer(modifier = Modifier.height(16.dp))

                ActivityGraphSection(
                    selectedPeriod = selectedPeriod,
                    dailyData = dailyData,
                    monthlyData = monthlyData,
                    totalData = totalData
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 컨디션 상세로 이동
                ConditionLevelCard(
                    conditionLevel = 72,
                    onClick = { navController.navigate("active/condition") }
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "최근 활동",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentActivities.forEach { ActivityItem(it) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                GoalSection(
                    progress = goalProgress,
                    onClick = { navController.navigate("active/goal") }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ====== 상단 통계 카드 ======

@Composable
fun StatsCard(stats: RunningStats) {
    // java.time 대신 Date + SimpleDateFormat 사용 (API 24 OK)
    val today = remember { Date() }
    val formatter = remember {
        SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREAN)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = formatter.format(today),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = String.format(Locale.getDefault(), "%.1f", stats.totalDistanceKm),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = "킬로미터",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "러닝", value = "${stats.runCount}회")
                StatItem(label = "평균 페이스", value = stats.avgPace)
                StatItem(label = "평균 심박수", value = "${stats.avgHeartRate}bpm")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF558B2F))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
    }
}

// ====== 기간 선택 버튼 ======

@Composable
fun PeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodButton("전체", selectedPeriod == PeriodType.ALL) {
            onPeriodSelected(PeriodType.ALL)
        }
        PeriodButton("년", selectedPeriod == PeriodType.YEAR) {
            onPeriodSelected(PeriodType.YEAR)
        }
        PeriodButton("월", selectedPeriod == PeriodType.MONTH) {
            onPeriodSelected(PeriodType.MONTH)
        }
    }
}

@Composable
fun RowScope.PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF558B2F) else Color(0xFFC8E6C9),
            contentColor = if (isSelected) Color.White else Color(0xFF2E7D32)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}

// ====== 활동 그래프 섹션 ======

@Composable
fun ActivityGraphSection(
    selectedPeriod: PeriodType,
    dailyData: List<DailyActivityData>,
    monthlyData: List<MonthlyActivityData>,
    totalData: List<TotalActivityData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = when (selectedPeriod) {
                    PeriodType.ALL -> "전체 활동"
                    PeriodType.YEAR -> "월별 활동"
                    PeriodType.MONTH -> "일별 활동"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedPeriod) {
                PeriodType.MONTH -> {
                    DailyActivityChart(
                        data = dailyData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                PeriodType.YEAR -> {
                    MonthlyActivityChart(
                        data = monthlyData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                PeriodType.ALL -> {
                    TotalActivityChart(
                        data = totalData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyActivityChart(data: List<DailyActivityData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 100f
        val chartHeight = size.height - 80f
        val maxValue = 20f // 최대 20km 정도로 가정
        val barWidth = (chartWidth / 31f) * 0.7f

        // Y축 그리드 (라벨 텍스트는 생략)
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(80f, y),
                end = Offset(chartWidth + 80f, y),
                strokeWidth = 1f
            )
        }

        data.forEach { activity ->
            val x = 80f + (chartWidth * (activity.day - 1) / 30f)
            val barHeight = ((activity.distanceKm / maxValue) * chartHeight).toFloat()
            val barY = chartHeight - barHeight + 20f

            drawRect(
                color = Color(0xFF558B2F),
                topLeft = Offset(x, barY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun MonthlyActivityChart(data: List<MonthlyActivityData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 100f
        val chartHeight = size.height - 80f
        val maxValue = 150f
        val barWidth = (chartWidth / 12f) * 0.7f

        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(80f, y),
                end = Offset(chartWidth + 80f, y),
                strokeWidth = 1f
            )
        }

        data.forEach { activity ->
            val x = 80f + (chartWidth * (activity.month - 1) / 11f)
            val barHeight = ((activity.distanceKm / maxValue) * chartHeight).toFloat()
            val barY = chartHeight - barHeight + 20f

            drawRect(
                color = Color(0xFF558B2F),
                topLeft = Offset(x, barY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun TotalActivityChart(data: List<TotalActivityData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 120f
        val chartHeight = size.height - 80f
        val maxValue = 400f
        val barWidth = if (data.isNotEmpty()) (chartWidth / data.size) * 0.7f else 20f

        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(100f, y),
                end = Offset(chartWidth + 100f, y),
                strokeWidth = 1f
            )
        }

        data.forEachIndexed { index, activity ->
            val x = 100f + (chartWidth * index / data.size.coerceAtLeast(1))
            val barHeight = ((activity.distanceKm / maxValue) * chartHeight).toFloat()
            val barY = chartHeight - barHeight + 20f

            drawRect(
                color = Color(0xFF558B2F),
                topLeft = Offset(x, barY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

// ====== 컨디션 카드 & 목표 카드 / 리스트 ======

@Composable
fun ConditionLevelCard(
    conditionLevel: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "컨디션 레벨 지수",
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = conditionLevel.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = " / 100",
                    fontSize = 14.sp,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityItem(activity: RunningData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = activity.dateLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
            Text(
                text = "${activity.calories}kcal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = String.format(Locale.getDefault(), "%.1fkm", activity.distanceKm),
                fontSize = 14.sp,
                color = Color(0xFF558B2F)
            )
        }
    }
}

@Composable
fun GoalSection(
    progress: Double,
    onClick: () -> Unit
) {
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("목표 달성률", fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$percentage%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "이달의 목표: 100km",
                fontSize = 12.sp,
                color = Color(0xFFE8F5E9)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFC8E6C9),
                trackColor = Color(0xFF2E7D32)
            )
        }
    }
}
