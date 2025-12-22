package com.example.runnershigh.ui.screen.active

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

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
@Suppress("UNUSED_PARAMETER")
fun ActiveScreen(
    navController: NavController,
    userUuid: String,
    userHeightCm: Double?,
    userWeightKg: Double?,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    recentActivities: List<RunningData> = emptyList(),
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val scrollState = rememberScrollState()
    var selectedPeriod by remember { mutableStateOf(PeriodType.ALL) }

    LaunchedEffect(userUuid, userHeightCm, userWeightKg) {
        if (userUuid.isNotBlank()) {
            viewModel.loadActivityData(
                userUuid = userUuid,
                userHeightCm = userHeightCm,
                userWeightKg = userWeightKg
            )
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    val stats = uiState.currentStats
    val dailyData = uiState.dailyData
    val monthlyData = uiState.monthlyData
    val totalData = uiState.totalData
    val recentActivities = uiState.recentActivities
    val goalProgress = uiState.goalProgress
    val conditionScore = uiState.conditionScore ?: uiState.conditionLevel
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text("활동") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF1B5E20)
                    )
                }
            },
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
            if (!errorMessage.isNullOrBlank()) {
                ErrorMessage(errorMessage)
                Spacer(modifier = Modifier.height(12.dp))
            }

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
                    conditionLevel = conditionScore,
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

@Composable
private fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFD84315)
        )
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
        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = Color(0xFF1B5E20).toArgb()
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }

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

            if (activity.distanceKm > 0) {
                val label = String.format(Locale.getDefault(), "%.1f", activity.distanceKm)
                val textX = x + (barWidth / 2f)
                val textY = max(24f, barY - 8f)
                drawContext.canvas.nativeCanvas.drawText(label, textX, textY, labelPaint)
            }
        }
    }
}

@Composable
fun TotalActivityChart(data: List<TotalActivityData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 120f
        val chartHeight = size.height - 80f
        val maxDistance = data.maxOfOrNull { it.distanceKm } ?: 0.0
        val maxValue = (maxDistance * 1.2).coerceAtLeast(10.0).toFloat()
        val barWidth = if (data.isNotEmpty()) (chartWidth / data.size) * 0.7f else 20f
        val minVisibleBarHeight = 6f
        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = Color(0xFF1B5E20).toArgb()
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }

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
            val rawBarHeight = ((activity.distanceKm / maxValue) * chartHeight).toFloat()
            val barHeight = if (activity.distanceKm > 0) {
                rawBarHeight.coerceAtLeast(minVisibleBarHeight)
            } else {
                0f
            }
            val barY = chartHeight - barHeight + 20f

            drawRect(
                color = Color(0xFF558B2F),
                topLeft = Offset(x, barY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )

            if (activity.distanceKm > 0) {
                val label = String.format(Locale.getDefault(), "%.1f", activity.distanceKm)
                val textX = x + (barWidth / 2f)
                val textY = max(24f, barY - 8f)
                drawContext.canvas.nativeCanvas.drawText(label, textX, textY, labelPaint)
            }
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
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE8F5E9)
            )
            Spacer(modifier = Modifier.height(6.dp))
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
