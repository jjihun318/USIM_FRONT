package com.example.runnershigh.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnershigh.domain.model.RunningStats
import com.example.runnershigh.ui.theme.RacingSansOne
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ActiveRunningScreen(
    onStop: () -> Unit,
    onMenuClick: () -> Unit = {},
    onFinish: (RunningStats) -> Unit = {}
) {
    // 비교 오버레이 표시 여부
    var showOverlay by remember { mutableStateOf(false) }

    // 러닝 진행 여부 (true = 진행중, false = 일시정지)
    var isRunning by remember { mutableStateOf(true) }

    // 러닝 데이터 (나중에 Health Connect 값으로 교체)
    var seconds by remember { mutableIntStateOf(0) }
    var distanceKm by remember { mutableDoubleStateOf(6.0) }   // 예시: 현재 6km
    var calories by remember { mutableIntStateOf(404) }
    var bpm by remember { mutableIntStateOf(160) }

    val currentPaceSecPerKm = 301 // 5:01
    val targetPaceSecPerKm = 283  // 4:43
    val targetDistanceKm = 10.0   // 목표 10km

    // isRunning 이 true 일 때만 타이머 작동
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            seconds += 1
            // TODO: distanceKm / calories / bpm 업데이트
        }
    }

    val timeText = formatTime(seconds)
    val paceText = formatPace(currentPaceSecPerKm)

    // 뒤로가기: 오버레이가 떠 있을 땐 먼저 오버레이만 닫기
    BackHandler(enabled = showOverlay) {
        showOverlay = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ───────── 왼쪽: 러닝 타이틀 + 지도 영역 ─────────
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 24.dp, top = 24.dp, end = 120.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Runner's High.",
                    fontFamily = RacingSansOne,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { showOverlay = true },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "이 부분은 네이버 지도 API로\n현재 위치 표시.",
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        color = Color.Black
                    )
                }
            }

            // ───────── 오른쪽 초록색 정보 패널 ─────────
            RightStatsPanel(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(96.dp)
                    .align(Alignment.CenterEnd),
                timeText = timeText,
                calories = calories,
                paceText = paceText,
                distanceKm = distanceKm,
                elevationText = "25M",
                bpm = bpm,
                onMenuClick = onMenuClick
            )

            // ───────── 하단 Stop / Pause 버튼 ─────────
            BottomControlButtons(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                isRunning = isRunning,
                onTogglePause = { isRunning = !isRunning },
                onStopLongPress = {
                    // 길게 눌렀을 때 러닝 종료 처리
                    val stats = RunningStats(
                        distanceKm = distanceKm,
                        durationSec = seconds,
                        paceSecPerKm = currentPaceSecPerKm,
                        calories = calories,
                        avgHeartRate = bpm,
                        elevationGainM = 25,
                        cadence = 160
                    )
                    onFinish(stats)   // 결과 전달
                    onStop()          // 상위 화면 상태 초기화
                }
            )

            // ───────── 비교 오버레이 ─────────
            if (showOverlay) {
                RunningStatsOverlayScreen(
                    currentPaceSecPerKm = currentPaceSecPerKm,
                    targetPaceSecPerKm = targetPaceSecPerKm,
                    currentDistanceKm = distanceKm,
                    targetDistanceKm = targetDistanceKm,
                    elapsedSeconds = seconds,
                    onBack = { showOverlay = false }
                )
            }
        }
    }
}

/* ===== 패널 / 버튼 / 유틸 함수 ===== */

@Composable
private fun RightStatsPanel(
    modifier: Modifier = Modifier,
    timeText: String,
    calories: Int,
    paceText: String,
    distanceKm: Double,
    elevationText: String,
    bpm: Int,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = modifier.background(Color(0xFF73F212)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onMenuClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = timeText, subtitle = "")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = calories.toString(), subtitle = "Kcal")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = paceText, subtitle = "Pace")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(
            title = String.format(Locale.getDefault(), "%.2f", distanceKm),
            subtitle = "KM"
        )
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = elevationText, subtitle = "고도")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = bpm.toString(), subtitle = "BPM")
    }
}

@Composable
private fun StatItem(
    title: String,
    subtitle: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontFamily = RacingSansOne,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                fontFamily = RacingSansOne,
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BottomControlButtons(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    onTogglePause: () -> Unit,
    onStopLongPress: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 정지 버튼 (길게 누르기)
        CircleButton(
            icon = Icons.Default.Stop,
            contentDescription = "Stop",
            onClick = null,
            onLongPress = onStopLongPress
        )

        // 일시정지 / 재생 버튼
        CircleButton(
            icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isRunning) "Pause" else "Resume",
            onClick = onTogglePause
        )
    }
}

@Composable
private fun CircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .size(96.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        color = Color(0xFF73F212)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onClick, onLongPress) {
                    detectTapGestures(
                        onLongPress = {
                            onLongPress?.invoke()
                        },
                        onTap = {
                            onClick?.invoke()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

/* ---- 포맷팅 함수 ---- */

private fun formatTime(totalSeconds: Int): String {
    val min = totalSeconds / 60
    val sec = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}

private fun formatPace(secPerKm: Int): String {
    val min = secPerKm / 60
    val sec = secPerKm % 60
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}
