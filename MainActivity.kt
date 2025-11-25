package com.example.capstone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

// 사용되지 않는 import (혹시 몰라서 놔둠)
//import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.lazy.items

data class HRVData(
    val date: String,
    val value: Int
)

// 페이스 비교 데이터
data class PaceComparisonData(
    val date: String,
    val targetPace: Int, // 초 단위
    val actualPace: Int  // 초 단위
)

// 심박수 구간 데이터
data class HeartRateZoneData(
    val zoneName: String,
    val range: String,
    val percentage: Int,
    val color: Color
)

// 목표별 피드백 카테고리
data class GoalFeedbackItem(
    val icon: String,
    val title: String,
    val score: Int,
    val description: String,
    val color: Color
)

sealed class Screen {
    object Main : Screen()
    object GoalDetail : Screen()
    object ConditionDetail : Screen()
}

// 러닝 데이터 모델
data class RunningData(
    val date: LocalDate,
    val distance: Double,
    val duration: Int,
    val avgHeartRate: Int,
    val calories: Int
) {
    fun getPacePerKm(): String {
        val totalSeconds = duration * 60
        val secondsPerKm = totalSeconds / distance
        val minutes = (secondsPerKm / 60).toInt()
        val seconds = (secondsPerKm % 60).toInt()
        return String.format("%d'%02d\"", minutes, seconds)
    }
}

enum class PeriodType { ALL, YEAR, MONTH }

data class RunningStats(
    val totalDistance: Double,
    val runCount: Int,
    val avgPace: String,
    val avgHeartRate: Int
)

// 컨디션 데이터
data class InjuryData(
    val part: String,
    val percentage: Int,
    val hosoCount: Int = 0,
    val severity: String = "보통"
)

data class PaceDeclineData(
    val distance: String,
    val severity: SeverityLevel
)

enum class SeverityLevel {
    LOW, MEDIUM, HIGH
}

data class WeeklyConditionScore(
    val week: String,
    val score: Int
)

// 사용자 목표
enum class UserGoal(val displayName: String) {
    MARATHON("마라톤 준비"),
    DIET("다이어트"),
    FITNESS("격투기, 체력 증진"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

            // 뒤로가기 처리
            BackHandler(enabled = currentScreen != Screen.Main) {
                currentScreen = Screen.Main
            }

            when (currentScreen) {
                Screen.Main -> FitnessScreen(
                    onGoalClick = { currentScreen = Screen.GoalDetail },
                    onConditionClick = { currentScreen = Screen.ConditionDetail }
                )
                Screen.GoalDetail -> GoalDetailScreen(
                    onBackClick = { currentScreen = Screen.Main }
                )
                Screen.ConditionDetail -> ConditionDetailScreen(
                    onBackClick = { currentScreen = Screen.Main }
                )
            }
        }
    }
}

// 새로운 데이터 모델
data class FeedbackCategory(
    val icon: String,
    val color: Color,
    val backgroundColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val tips: List<String>
)

@Composable
fun ImprovementSection(injuryData: List<InjuryData>, paceDeclineData: List<PaceDeclineData>, hrvData: List<HRVData>) {
    val mostInjuredPart = injuryData.maxByOrNull { injury -> injury.percentage }?.part ?: "무릎"
    val hasPaceIssue = paceDeclineData.any { pace -> pace.severity == SeverityLevel.HIGH }
    val currentHRV = hrvData.lastOrNull()?.value ?: 80

    val feedbackCategories = listOf(
        FeedbackCategory(
            icon = "⚠️",
            color = Color(0xFFF44336),
            backgroundColor = Color(0xFFFFEBEE),
            title = "부상 예방",
            subtitle = "$mostInjuredPart 부상 주의",
            description = "${mostInjuredPart}에 부담이 집중되고 있습니다. 적절한 휴식과 스트레칭을 병행하고, 무릎 보호대를 착용하세요.",
            tips = listOf(
                "실천 방법",
                "· 워밍업 10분 이상",
                "· 런지 및 스쿼트 강화",
                "· 충격 흡수 좋은 러닝화 사용"
            )
        ),
        FeedbackCategory(
            icon = "⚡",
            color = Color(0xFFFF9800),
            backgroundColor = Color(0xFFFFF3E0),
            title = "페이스 관리",
            subtitle = if (hasPaceIssue) "후반 페이스가 하락" else "페이스 유지 중",
            description = if (hasPaceIssue) "4km 이후 페이스가 급격히 느려집니다. 초반 페이스를 조금 늦추고 후반 체력을 아껴보세요."
            else "안정적인 페이스를 유지하고 있습니다.",
            tips = listOf(
                "실천 방법",
                "· 초반 페이스 5-10초 늦추기",
                "· 간격별 트레이닝 추가",
                "· 장거리 러닝 빈도 늘리기"
            )
        ),
        FeedbackCategory(
            icon = "💙",
            color = Color(0xFF2196F3),
            backgroundColor = Color(0xFFE3F2FD),
            title = "회복력",
            subtitle = if (currentHRV >= 70) "회복 능력 향상 중" else "회복 관리 필요",
            description = if (currentHRV >= 70) "심박수 변동성이 증가하고 있어 회복 능력이 좋아지고 있습니다. 이 상태를 계속 유지하세요."
            else "심박수 변동성이 낮아지고 있습니다. 충분한 휴식이 필요합니다.",
            tips = listOf(
                "유지 방법",
                "· 충분한 수면 (7-8시간)",
                "· 고단백 후 회복 식사",
                "· 영양 섭취 관리"
            )
        )
    )

    Column {
        Text(
            text = "맞춤형 피드백",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(12.dp))

        feedbackCategories.forEach { category ->
            FeedbackCard(category = category)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun FeedbackCard(category: FeedbackCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = category.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = category.icon,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = category.title,
                        fontSize = 14.sp,
                        color = category.color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = category.subtitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.description,
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 14.sp
                        )
                        Text(
                            text = category.tips[0],
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    category.tips.drop(1).forEach { tip ->
                        Text(
                            text = tip,
                            fontSize = 12.sp,
                            color = Color(0xFF424242),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HRVSection(hrvData: List<HRVData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    .height(280.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "높을수록 회복력이 좋습니다",
                        fontSize = 13.sp,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HRVLineChart(
                        data = hrvData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "현재 HRV: ${hrvData.lastOrNull()?.value ?: 0} (우수)",
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
        val maxValue = 100f
        val minValue = 0f

        // Y축 그리드 라인 및 레이블
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = (minValue + (maxValue - minValue) * i / 4).toInt()

            // 그리드 라인
            drawLine(
                color = Color(0xFFBBDEFB),
                start = Offset(60f, y),
                end = Offset(chartWidth + 60f, y),
                strokeWidth = 1f
            )

            // Y축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                20f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = Color(0xFF1976D2).toArgb()
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // 데이터 포인트 계산
        val points = data.mapIndexed { index, hrvData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            val normalizedValue = (hrvData.value - minValue) / (maxValue - minValue)
            val y = chartHeight - (chartHeight * normalizedValue) + 20f
            Offset(x, y)
        }

        // 라인 그리기
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF4CAF50),
                style = Stroke(width = 6f)
            )
        }

        // 데이터 포인트 그리기
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 8f,
                center = point
            )
        }

        // X축 레이블
        data.forEachIndexed { index, hrvData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            drawContext.canvas.nativeCanvas.drawText(
                hrvData.date,
                x,
                chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = Color(0xFF1976D2).toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionDetailScreen(onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()

    // 선택된 기간
    var selectedPeriod by remember { mutableStateOf(PeriodType.ALL) }

    // 컨디션 레벨 (계산된 값)
    val conditionLevel = 90

    // 주요 부상 호소 부위 (사용자가 선택한 통증 부위 데이터)
    // 주요 부상 호소 부위 (사용자가 선택한 통증 부위 데이터)
    val injuryData = remember(selectedPeriod) {
        // 실제로는 선택된 기간에 따라 DB에서 가져와야 함
        listOf(
            InjuryData("무릎", 40, 8, "주의"),
            InjuryData("발목", 25, 5, "보통"),
            InjuryData("허벅지", 20, 4, "보통"),
            InjuryData("종아리", 15, 3, "보통")
        )
    }

    // 페이스 하락 구간
    val paceDeclineData = listOf(
        PaceDeclineData("0-2km", SeverityLevel.LOW),
        PaceDeclineData("2-4km", SeverityLevel.LOW),
        PaceDeclineData("4-6km", SeverityLevel.MEDIUM),
        PaceDeclineData("6-8km", SeverityLevel.HIGH),
        PaceDeclineData("8-10km", SeverityLevel.MEDIUM)
    )

    // 주간 컨디션 점수 (최근 5주)
    val weeklyScores = listOf(
        WeeklyConditionScore("5주 전", 75),
        WeeklyConditionScore("4주 전", 78),
        WeeklyConditionScore("3주 전", 83),
        WeeklyConditionScore("2주 전", 87),
        WeeklyConditionScore("이번 주", 90)
    )

    // 향상 제안
    val improvementSuggestions = listOf(
        ImprovementSuggestion(
            "부상 예방 가이드",
            "달리기 전후 스트레칭을 통한 부상 예방",
            SeverityLevel.HIGH
        ),
        ImprovementSuggestion(
            "회복 가이드",
            "전기 자극 치료로 근육 회복 촉진",
            SeverityLevel.MEDIUM
        ),
        ImprovementSuggestion(
            "온·냉찜질",
            "냉찜질로 염증 감소 후 온찜질로 혈액순환",
            SeverityLevel.LOW
        ),
        ImprovementSuggestion(
            "추천 훈련 가이드",
            "수영이나 사이클링으로 무릎 부담 감소",
            SeverityLevel.MEDIUM
        )
    )

    // ConditionDetailScreen 함수 내부, improvementSuggestions 아래에 추가
    val hrvData = listOf(
        HRVData("10/12", 65),
        HRVData("10/13", 68),
        HRVData("10/14", 70),
        HRVData("10/15", 62),
        HRVData("10/16", 73),
        HRVData("10/17", 77),
        HRVData("10/18", 80)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바
        TopAppBar(
            title = { Text("컨디션 레벨 분석") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            // 컨디션 레벨 지수
            ConditionScoreCard(score = conditionLevel)

            Spacer(modifier = Modifier.height(16.dp))

            // 기간 선택
            Text(
                text = "주요 통증 호소 부위",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 부상 부위 통계
            InjuryStatsCard(injuryData = injuryData)
            Spacer(modifier = Modifier.height(16.dp))

            // 페이스 하락 구간
            PaceDeclineSection(paceDeclineData = paceDeclineData)
            Spacer(modifier = Modifier.height(16.dp))

            HRVSection(hrvData = hrvData)
            Spacer(modifier = Modifier.height(16.dp))

            // 향상 제안
            ImprovementSection(
                injuryData = injuryData,
                paceDeclineData = paceDeclineData,
                hrvData = hrvData
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 주간 컨디션 점수
            WeeklyConditionSection(weeklyScores = weeklyScores)
            Spacer(modifier = Modifier.height(16.dp))

            // 종합 평가
            ComprehensiveEvaluation()
        }
    }
}

@Composable
fun ConditionLevelCard(conditionLevel: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // 높이 증가
            .clickable(onClick = onClick), // 클릭 기능 유지
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
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
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

data class ImprovementSuggestion(
    val title: String,
    val description: String,
    val severity: SeverityLevel
)

@Composable
fun ConditionScoreCard(score: Int) {
    val conditionHistory = listOf(
        WeeklyConditionScore("10/12", 75),
        WeeklyConditionScore("10/13", 78),
        WeeklyConditionScore("10/14", 83),
        WeeklyConditionScore("10/15", 80),
        WeeklyConditionScore("10/16", 87),
        WeeklyConditionScore("10/17", 88),
        WeeklyConditionScore("10/18", 90)
    )

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "컨디션 레벨 지수",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Icon(
                    imageVector = Icons.Default.ArrowBack, // 임시로 ArrowBack 사용
                    contentDescription = "추세",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = score.toString(),
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

            Text(
                text = "건강한 러닝을 유지하고 있습니다 ✨",
                fontSize = 13.sp,
                color = Color(0xFF558B2F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConditionLineChart(
                data = conditionHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
fun ConditionLineChart(data: List<WeeklyConditionScore>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val chartWidth = size.width - 80f
        val chartHeight = size.height - 40f
        val maxValue = 100f
        val minValue = 0f

        // Y축 그리드 라인 및 레이블 (0, 25, 50, 75, 100)
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = i * 25

            // 그리드 라인
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(60f, y),
                end = Offset(chartWidth + 60f, y),
                strokeWidth = 1f
            )

            // Y축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                20f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // 데이터 포인트 계산
        val points = data.mapIndexed { index, scoreData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            val normalizedValue = (scoreData.score - minValue) / (maxValue - minValue)
            val y = chartHeight - (chartHeight * normalizedValue) + 20f
            Offset(x, y)
        }

        // 라인 그리기
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF2196F3),
                style = Stroke(width = 6f)
            )
        }

        // 데이터 포인트 그리기
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF2196F3),
                radius = 8f,
                center = point
            )
        }

        // X축 레이블 (날짜)
        data.forEachIndexed { index, scoreData ->
            val x = 60f + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
            val displayDate = scoreData.week.replace("주 전", "").replace("이번 주", "10/18")
            drawContext.canvas.nativeCanvas.drawText(
                displayDate,
                x,
                chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun InjuryStatsCard(injuryData: List<InjuryData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        injuryData.forEach { injury ->
            InjuryProgressBar(injury)
            Spacer(modifier = Modifier.height(16.dp))
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
        "허벅지" -> "🦵"
        "종아리" -> "🦵"
        "발바닥" -> "🦶"
        "정강이" -> "🦵"
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
            // 아이콘
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            // 중간 내용
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${injury.percentage}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
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
                    text = "${injury.hosoCount}회 호소",
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

@Composable
fun PaceDeclineSection(paceDeclineData: List<PaceDeclineData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "페이스 하락 구간",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(16.dp))

        paceDeclineData.forEach { data ->
            PaceDeclineCard(data = data)
            Spacer(modifier = Modifier.height(12.dp))
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

    val (pace, paceChange) = when (data.severity) {
        SeverityLevel.LOW -> Pair("5'48\"", null)
        SeverityLevel.MEDIUM -> Pair("6'02\"", "+17 sec")
        SeverityLevel.HIGH -> Pair("6'28\"", "+26 sec")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 거리 구간
            Column {
                Text(
                    text = data.distance,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "평균: $pace",
                    fontSize = 13.sp,
                    color = Color(0xFF558B2F)
                )
            }

            // 오른쪽: 상태
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                paceChange?.let {
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

@Composable
fun WeeklyConditionSection(weeklyScores: List<WeeklyConditionScore>) {
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
                text = "주간 컨디션 점수",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            weeklyScores.forEach { score ->
                WeeklyScoreItem(score = score)
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
            Text(
                text = score.week,
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )
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
fun ComprehensiveEvaluation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF558B2F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "종합 평가",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "전반적으로 컨디션이 좋습니다! 무릎과 발목에 약간의 피로가 누적되고 있으니 스트레칭과 충분한 휴식을 취하세요. 페이스 조절에 신경 쓰면 더 안정적인 러닝이 가능할 것입니다.",
                fontSize = 14.sp,
                color = Color(0xFFE8F5E9),
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(onGoalClick: () -> Unit, onConditionClick: () -> Unit) {
    val scrollState = rememberScrollState()

    // 현재 날짜를 가져옴
    val today = remember { LocalDate.now() }

    val allRunningData = remember(today) {
        listOf(
            RunningData(today.minusDays(4), 5.2, 30, 145, 483),
            RunningData(today.minusDays(3), 3.5, 20, 138, 220),
            RunningData(today.minusDays(2), 6.8, 40, 152, 520),
            RunningData(today.minusDays(1), 4.2, 25, 148, 350),
            RunningData(today, 2.1, 15, 135, 138),
        )
    }

    var selectedPeriod by remember { mutableStateOf(PeriodType.ALL) }

    val filteredData = remember(selectedPeriod) {
        val now = LocalDate.now()
        when (selectedPeriod) {
            PeriodType.ALL -> allRunningData
            PeriodType.YEAR -> allRunningData.filter { it.date.year == now.year }
            PeriodType.MONTH -> allRunningData.filter {
                it.date.year == now.year && it.date.month == now.month
            }
        }
    }

    val stats = remember(filteredData) {
        if (filteredData.isEmpty()) {
            RunningStats(0.0, 0, "0'00\"", 0)
        } else {
            val totalDistance = filteredData.sumOf { it.distance }
            val totalDuration = filteredData.sumOf { it.duration }
            val avgPaceSeconds = (totalDuration * 60) / totalDistance
            val avgPaceMin = (avgPaceSeconds / 60).toInt()
            val avgPaceSec = (avgPaceSeconds % 60).toInt()
            val avgPace = String.format("%d'%02d\"", avgPaceMin, avgPaceSec)
            val avgHR = filteredData.map { it.avgHeartRate }.average().toInt()

            RunningStats(totalDistance, filteredData.size, avgPace, avgHR)
        }
    }

    val goalProgress = remember(stats) {
        val monthlyGoal = 100.0
        (stats.totalDistance / monthlyGoal).coerceIn(0.0, 1.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바 추가
        TopAppBar(
            title = { Text("활동") },
            navigationIcon = {
                IconButton(onClick = { /* 뒤로가기 동작 */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            StatsCard(stats = stats)
            Spacer(modifier = Modifier.height(24.dp))

            // 컨디션 레벨 카드 (클릭 가능)
            ConditionLevelCard(
                conditionLevel = 90,
                onClick = onConditionClick
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "최근 활동",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // LazyColumn에서 일반 Column으로 변경
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredData.sortedByDescending { it.date }.take(5).forEach { activity ->
                    ActivityItem(activity)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 목표 달성률 UI 개선
            GoalSection(
                progress = goalProgress,
                onClick = onGoalClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()

    // 사용자 목표 (실제로는 사용자 설정에서 가져와야 함)
    val userGoal = remember { UserGoal.MARATHON }

    // 동적 데이터들
    val goalProgress = 92 // 실제 계산값
    val weeklyDistance = 6.8 // km
    val weeklyRuns = 4
    val avgPace = "6'07\""
    val avgHeartRate = 145

    // 페이스 유지력 분석 데이터
    val paceComparisonData = listOf(
        PaceComparisonData("10/09", 360, 380),
        PaceComparisonData("10/11", 360, 370),
        PaceComparisonData("10/12", 360, 390),
        PaceComparisonData("10/14", 360, 370),
        PaceComparisonData("10/16", 360, 380),
        PaceComparisonData("10/18", 360, 350)
    )

// 심박수 구간 분석 데이터
    val heartRateZones = listOf(
        HeartRateZoneData("회복 구간", "120-130 bpm", 15, Color(0xFF4CAF50)),
        HeartRateZoneData("유산소 구간", "131-145 bpm", 45, Color(0xFF2196F3)),
        HeartRateZoneData("템포 구간", "146-160 bpm", 30, Color(0xFFFF9800)),
        HeartRateZoneData("고강도 구간", "161+ bpm", 10, Color(0xFFF44336))
    )

// 목표별 마라톤 피드백
    val marathonFeedback = listOf(
        GoalFeedbackItem(
            icon = "🎯",
            title = "페이스 유지력",
            score = 95,
            description = "목표 페이스를 잘지키며 꾸준하게 달렸습니다. 페이스 변동폭이 줄어 안정감이 있는 달리기를 하고 있습니다.",
            color = Color(0xFF4CAF50)
        ),
        GoalFeedbackItem(
            icon = "💙",
            title = "심박수 관리",
            score = 88,
            description = "유산소 구간에서 45%의 시간을 훈련한 것은 지구력 향상에 좋습니다. 다음 훈련에서는 템포 구간 훈련을 조금씩 늘려보세요.",
            color = Color(0xFF2196F3)
        ),
        GoalFeedbackItem(
            icon = "⚡",
            title = "거리 달성",
            score = 92,
            description = "목표 거리의 92%를 달성하며 우수합니다. 주간 훈련량을 6.8km로 꾸준히 유지해보세요.",
            color = Color(0xFFFF9800)
        )
    )

// 향상 제안 (목표별로 달라짐)
    val improvementSuggestions = when (userGoal) {
        UserGoal.MARATHON -> listOf(
            "주말 장거리 러닝 추가",
            "인터벌 트레이닝 도입으로 페이스 감각 키우기",
            "회복 러닝 시간 늘리기"
        )
        UserGoal.DIET -> listOf(
            "심박수 130-145 구간 유지하기",
            "주 5회 이상 꾸준한 러닝",
            "러닝 후 단백질 섭취"
        )
        UserGoal.FITNESS -> listOf(
            "고강도 인터벌 트레이닝",
            "언덕 러닝 추가",
            "크로스 트레이닝 병행"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바
        TopAppBar(
            title = { Text("목표 달성 분석") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            // 목표 달성률
            GoalProgressCard(progress = goalProgress)

            Spacer(modifier = Modifier.height(16.dp))

            // 이번주 기록
            WeeklyRecordCard(
                distance = weeklyDistance,
                runs = weeklyRuns,
                avgPace = avgPace,
                avgHeartRate = avgHeartRate
            )

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // 페이스 유지력 분석
            PaceMaintenanceCard(data = paceComparisonData)

            Spacer(modifier = Modifier.height(16.dp))

            // 심박수 구간 분석
            HeartRateZoneCard(zones = heartRateZones)

            Spacer(modifier = Modifier.height(16.dp))

            // 마라톤 피드백
            MarathonFeedbackCard(userGoal = userGoal, feedbackItems = marathonFeedback)

            Spacer(modifier = Modifier.height(16.dp))

            // 향상 제안
            ImprovementSuggestionsCard(suggestions = improvementSuggestions)

            Spacer(modifier = Modifier.height(16.dp))

            // 전체 평가
            OverallEvaluationCard(userGoal = userGoal, progress = goalProgress)
        }
    }
}


@Composable
fun GoalProgressCard(progress: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
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
                text = "목표 달성률",
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$progress%",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFC8E6C9),
                trackColor = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun WeeklyRecordCard(
    distance: Double,
    runs: Int,
    avgPace: String,
    avgHeartRate: Int
) {
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
                text = "월간 평균 통계",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(label = "거리", value = "${distance}km")
                MetricItem(label = "횟수", value = "${runs}회")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(label = "평균 페이스", value = avgPace)
                MetricItem(label = "평균 심박수", value = "${avgHeartRate}bpm")
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF558B2F)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
    }
}

@Composable
fun PaceMaintenanceCard(data: List<PaceComparisonData>) {
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
            // 1. 제목
            Text(
                text = "페이스 유지력 분석",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 2. 부제목
            Text(
                text = "목표 페이스 vs 실제 페이스 (초 단위)",
                fontSize = 13.sp,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. [수정됨] 범례 (Legend) UI 추가
            // 그래프와 겹치지 않도록 그래프 위에 별도로 그립니다.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // 오른쪽 정렬
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFFFFB74D), text = "목표 페이스") // 이미지의 노란색 계열
                Spacer(modifier = Modifier.width(12.dp))
                LegendItem(color = Color(0xFF42A5F5), text = "실제 페이스") // 이미지의 파란색 계열
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. 그래프
            PaceComparisonChart(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

// 범례 아이템을 그리는 간단한 컴포저블
@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun HeartRateZoneCard(zones: List<HeartRateZoneData>) {
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
                text = "심박수 구간 분석",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            zones.forEach { zone ->
                HeartRateZoneItem(zone = zone)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun HeartRateZoneItem(zone: HeartRateZoneData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = zone.zoneName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = zone.range,
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
                Text(
                    text = "${zone.percentage}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = zone.color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = zone.percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = zone.color,
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun MarathonFeedbackCard(userGoal: UserGoal, feedbackItems: List<GoalFeedbackItem>) {
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
                text = "${userGoal.displayName} 피드백",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(16.dp))

            feedbackItems.forEach { item ->
                GoalFeedbackItemCard(item = item)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun GoalFeedbackItemCard(item: GoalFeedbackItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.icon,
                    fontSize = 32.sp
                )
                Column {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                text = item.score.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = item.color
            )
        }
    }
}

@Composable
fun ImprovementSuggestionsCard(suggestions: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "향상 제안",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(12.dp))

            suggestions.forEach { suggestion ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = suggestion,
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PaceComparisonChart(data: List<PaceComparisonData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val chartWidth = size.width - 120f
        val chartHeight = size.height - 80f
        val maxValue = 400f
        // val minValue = 0f // 사용하지 않으므로 생략 가능
        val spacing = chartWidth / data.size
        val barWidth = spacing / 3f

        // 1. Y축 그리드 및 레이블
        for (i in 0..4) {
            val y = chartHeight - (chartHeight * i / 4f) + 20f
            val value = (i * 100).toString()

            // 그리드 라인
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(80f, y),
                end = Offset(chartWidth + 80f, y),
                strokeWidth = 1f
            )

            // Y축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                value,
                50f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // 2. 막대 그래프 그리기
        data.forEachIndexed { index, paceData ->
            val centerX = 80f + spacing * index + spacing / 2f

            // 목표 페이스 (주황색)
            val targetHeight = (paceData.targetPace / maxValue) * chartHeight
            val targetY = chartHeight - targetHeight + 20f
            drawRect(
                color = Color(0xFFFFB74D),
                topLeft = Offset(centerX - barWidth - 2f, targetY),
                size = androidx.compose.ui.geometry.Size(barWidth, targetHeight)
            )

            // 실제 페이스 (파란색)
            val actualHeight = (paceData.actualPace / maxValue) * chartHeight
            val actualY = chartHeight - actualHeight + 20f
            drawRect(
                color = Color(0xFF42A5F5),
                topLeft = Offset(centerX + 2f, actualY),
                size = androidx.compose.ui.geometry.Size(barWidth, actualHeight)
            )

            // X축 레이블
            drawContext.canvas.nativeCanvas.drawText(
                paceData.date,
                centerX,
                chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = Color(0xFF9E9E9E).toArgb()
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun OverallEvaluationCard(userGoal: UserGoal, progress: Int) {
    val evaluationText = when {
        progress >= 90 -> "현재 ${userGoal.displayName}이(가) 잘 진행되고 있습니다. 꾸준히 훈련을 이어가면 목표를 충분히 달성할 수 있습니다."
        progress >= 70 -> "현재 ${userGoal.displayName}이(가) 순조롭게 진행되고 있습니다. 조금 더 페이스를 유지하면 목표에 가까워질 것입니다."
        else -> "현재 ${userGoal.displayName}을(를) 위해 더 노력이 필요합니다. 훈련 빈도를 늘리고 일관성을 유지해보세요."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8CC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "전체 평가",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = evaluationText,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodButton("전체", selectedPeriod == PeriodType.ALL) { onPeriodSelected(PeriodType.ALL) }
        PeriodButton("년", selectedPeriod == PeriodType.YEAR) { onPeriodSelected(PeriodType.YEAR) }
        PeriodButton("월", selectedPeriod == PeriodType.MONTH) { onPeriodSelected(PeriodType.MONTH) }
    }
}

@Composable
fun RowScope.PeriodButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
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

@Composable
fun StatsCard(stats: RunningStats) {
    val today = remember { LocalDate.now() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", java.util.Locale.KOREAN) }

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
            // 오늘 날짜 추가
            Text(
                text = today.format(dateFormatter),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = String.format("%.1f", stats.totalDistance),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(text = "킬로미터", fontSize = 14.sp, color = Color(0xFF2E7D32))
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
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
    }
}

@Composable
fun ActivityItem(activity: RunningData) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", java.util.Locale.KOREAN)

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
                    text = activity.date.format(formatter),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = activity.date.format(dayFormatter),
                    fontSize = 12.sp,
                    color = Color(0xFF558B2F)
                )
            }
            Text(
                text = "${activity.calories}kcal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = "${activity.distance}km",
                fontSize = 14.sp,
                color = Color(0xFF558B2F)
            )
        }
    }
}

@Composable
fun GoalSection(progress: Double, onClick: () -> Unit) {
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick), // 클릭 가능하게
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
            Text(text = "목표 달성률", fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$percentage%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "이달의 목표: 100km", fontSize = 12.sp, color = Color(0xFFE8F5E9))
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