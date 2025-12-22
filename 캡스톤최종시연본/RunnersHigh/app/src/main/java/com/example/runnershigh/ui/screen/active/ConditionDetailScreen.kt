package com.example.runnershigh.ui.screen.active

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== 모델 =====

data class WeeklyConditionScore(
    val week: String,
    val score: Int
)

enum class SeverityLevel { LOW, MEDIUM, HIGH }

data class InjuryData(
    val part: String,
    val percentage: Int,
    val count: Int,
    val severity: String
)

data class PaceDeclineData(
    val distance: String,
    val severity: SeverityLevel,
    val avgPace: String,
    val paceChange: String?
)

data class HRVData(
    val date: String,
    val value: Int
)

// ===== 화면 =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionDetailScreen(
    onBackClick: () -> Unit,
    userUuid: String,
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val scrollState = rememberScrollState()
    val uiState = viewModel.uiState.collectAsState().value
    val conditionScore = (uiState.conditionScore ?: uiState.conditionLevel).takeIf { it > 0 }
    val conditionHistory = emptyList<WeeklyConditionScore>()
    val injuryData = emptyList<InjuryData>()
    val paceDecline = emptyList<PaceDeclineData>()
    val hrvData = emptyList<HRVData>()
    val weeklyScores = emptyList<WeeklyConditionScore>()
    val analysisText = uiState.conditionAnalysis

    LaunchedEffect(userUuid) {
        if (userUuid.isNotBlank()) {
            viewModel.loadActivityData(userUuid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text("컨디션 레벨 분석") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기"
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
            ConditionScoreCard(
                score = conditionScore,
                conditionHistory = conditionHistory
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "주요 통증 호소 부위",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))

            InjuryStatsCard(injuryData)
            Spacer(modifier = Modifier.height(16.dp))

            PaceDeclineSection(paceDecline)
            Spacer(modifier = Modifier.height(16.dp))

            HRVSection(hrvData)
            Spacer(modifier = Modifier.height(16.dp))

            WeeklyConditionSection(weeklyScores)
            Spacer(modifier = Modifier.height(16.dp))

            if (!analysisText.isNullOrBlank()) {
                ComprehensiveEvaluation(evaluationText = analysisText)
            }
        }
    }
}

// ====== 카드/그래프 컴포넌트들 ======

@Composable
fun ConditionScoreCard(
    score: Int?,
    conditionHistory: List<WeeklyConditionScore>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = score?.toString() ?: "—",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = " / 100",
                    fontSize = 16.sp,
                    color = Color(0xFF558B2F),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (conditionHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("데이터 없음", color = Color(0xFF9E9E9E))
                }
            } else {
                ConditionLineChart(
                    data = conditionHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
fun ConditionLineChart(
    data: List<WeeklyConditionScore>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val chartWidth = size.width - 80f
        val chartHeight = size.height - 40f
        val maxValue = 100f

        // Y축 그리드 + 라벨
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = i * 25

            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(60f, y),
                end = Offset(chartWidth + 60f, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                20f,
                y + 10f,
                Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 30f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        val points = data.mapIndexed { index, score ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            val normalized = (score.score / maxValue)
            val y = chartHeight - (chartHeight * normalized) + 20f
            Offset(x, y)
        }

        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
            }
            drawPath(
                path = path,
                color = Color(0xFF2196F3),
                style = Stroke(width = 6f)
            )
        }

        points.forEach { p ->
            drawCircle(
                color = Color(0xFF2196F3),
                radius = 8f,
                center = p
            )
        }
    }
}

// ===== 부상 카드 =====

@Composable
fun InjuryStatsCard(injuryData: List<InjuryData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (injuryData.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("데이터 없음", color = Color(0xFF9E9E9E))
                }
            }
        } else {
            injuryData.forEach {
                InjuryProgressBar(it)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InjuryProgressBar(injury: InjuryData) {
    val backgroundColor = when (injury.severity) {
        "주의" -> Color(0xFFFFF3E0)
        "보통" -> Color(0xFFE3F2FD)
        else -> Color(0xFFF5F5F5)
    }
    val progressColor = when (injury.severity) {
        "주의" -> Color(0xFFFF9800)
        "보통" -> Color(0xFF2196F3)
        else -> Color(0xFF9E9E9E)
    }
    val severityColor = when (injury.severity) {
        "주의" -> Color(0xFFFF9800)
        "보통" -> Color(0xFF4CAF50)
        else -> Color(0xFF9E9E9E)
    }

    val icon = when (injury.part) {
        "무릎" -> "🦵"
        "발목" -> "🦶"
        else -> "👤"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = injury.part,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${injury.percentage}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = severityColor)
                        ) {
                            Text(
                                text = injury.severity,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${injury.count}회 호소",
                    fontSize = 13.sp,
                    color = Color(0xFF558B2F)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = injury.percentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = progressColor,
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

// ===== 페이스 하락 카드 =====

@Composable
fun PaceDeclineSection(data: List<PaceDeclineData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "페이스 하락 구간",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (data.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("데이터 없음", color = Color(0xFF9E9E9E))
                }
            }
        } else {
            data.forEach {
                PaceDeclineCard(it)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PaceDeclineCard(data: PaceDeclineData) {
    val (statusText, statusColor, borderColor) = when (data.severity) {
        SeverityLevel.LOW -> Triple("정상", Color(0xFF4CAF50), Color(0xFF4CAF50))
        SeverityLevel.MEDIUM -> Triple("주의", Color(0xFFFF9800), Color(0xFFFF9800))
        SeverityLevel.HIGH -> Triple("개선 필요", Color(0xFFF44336), Color(0xFFF44336))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = data.distance,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "평균: ${data.avgPace}",
                    fontSize = 13.sp,
                    color = Color(0xFF558B2F)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                data.paceChange?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color(0xFF558B2F)
                    )
                }
            }
        }
    }
}

// ===== HRV 차트 =====

@Composable
fun HRVSection(hrvData: List<HRVData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "심박수 변동성 (HRV)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                HRVLineChart(
                    data = hrvData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentHRV = hrvData.lastOrNull()?.value ?: 0
            val status = when {
                currentHRV >= 70 -> "우수"
                currentHRV >= 50 -> "양호"
                currentHRV >= 30 -> "보통"
                else -> "주의"
            }

            Text(
                text = "현재 HRV: $currentHRV ($status)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
fun HRVLineChart(data: List<HRVData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val chartWidth = size.width - 80f
        val chartHeight = size.height - 40f

        if (data.isEmpty()) {
            drawContext.canvas.nativeCanvas.drawText(
                "데이터 없음",
                size.width / 2,
                size.height / 2,
                Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
            )
            return@Canvas
        }

        val maxValue = (data.maxOf { it.value }.toFloat()) + 10f
        val minValue = (data.minOf { it.value }.toFloat()) - 10f
        val range = maxValue - minValue

        // Grid Lines + Y Labels
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = (minValue + (range * i / 4)).toInt()

            drawLine(
                color = Color(0xFFBBDEFB),
                start = Offset(60f, y),
                end = Offset(chartWidth + 60f, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                20f,
                y + 10f,
                Paint().apply {
                    color = Color(0xFF1976D2).toArgb()
                    textSize = 30f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        val points = data.mapIndexed { index, d ->
            val x = 60f + (chartWidth * index / (data.size - 1))
            val normalized = (d.value - minValue) / range
            val y = chartHeight - (chartHeight * normalized) + 20f
            Offset(x, y)
        }

        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }

            drawPath(
                path = path,
                color = Color(0xFF4CAF50),
                style = Stroke(6f)
            )
        }

        points.forEach { p ->
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 8f,
                center = p
            )
        }

        // X축 라벨
        data.forEachIndexed { idx, d ->
            val x = 60f + (chartWidth * idx / (data.size - 1))
            drawContext.canvas.nativeCanvas.drawText(
                d.date,
                x,
                chartHeight + 50f,
                Paint().apply {
                    color = Color(0xFF1976D2).toArgb()
                    textSize = 26f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}

// ===== 주간 점수 / 종합 평가 =====

@Composable
fun WeeklyConditionSection(scores: List<WeeklyConditionScore>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "주간 컨디션 점수",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(16.dp))

            scores.forEach { score ->
                WeeklyScoreItem(score)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun WeeklyScoreItem(score: WeeklyConditionScore) {
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
            Text(score.week, fontSize = 14.sp, color = Color(0xFF2E7D32))
            Text(
                text = score.score.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
        }
    }
}

@Composable
fun ComprehensiveEvaluation(evaluationText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "종합 평가",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = evaluationText,
                fontSize = 14.sp,
                color = Color(0xFFE8F5E9),
                lineHeight = 20.sp
            )
        }
    }
}
