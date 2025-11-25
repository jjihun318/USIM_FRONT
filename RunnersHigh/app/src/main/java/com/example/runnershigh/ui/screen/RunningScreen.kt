package com.example.runnershigh.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.runnershigh.domain.model.RunningStats
import com.example.runnershigh.ui.RunningViewModel
import com.example.runnershigh.ui.theme.RacingSansOne

private enum class NavTab { LEVEL, RUNNING, START, COURSE, STATS }
private enum class ScreenState { NAVIGATION, COUNTDOWN, ACTIVE_RUNNING }

/**
 * 하단 네비게이션이 있는 메인 러닝 화면
 */
@Composable
fun RunningScreen(
    navController: NavController,
    runningViewModel: RunningViewModel,
    onMenuClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    // 러닝이 끝난 뒤 결과 화면으로 넘어갈 때 사용
    onRunningFinish: (RunningStats) -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(NavTab.RUNNING) }
    var screenState by remember { mutableStateOf(ScreenState.NAVIGATION) }

    // Start 버튼 → 세션 시작 + 카운트다운
    val handleStartClick = {
        runningViewModel.startSession()
        screenState = ScreenState.COUNTDOWN
    }

    val handleCountdownComplete = {
        screenState = ScreenState.ACTIVE_RUNNING
    }

    fun handleStopOnly() {
        screenState = ScreenState.NAVIGATION
        activeTab = NavTab.RUNNING
    }

    // 러닝 완전 종료(정지 버튼 길게 눌렀을 때)
    fun handleFinish(stats: RunningStats) {
        // 1) ViewModel 에 결과 저장 + 서버로 전송 시도
        runningViewModel.finishSession(stats)

        // 2) 상위에 알림 (NavGraph 에서 runningResult 로 네비게이션)
        onRunningFinish(stats)

        // 3) Running 탭 화면 상태 초기화
        screenState = ScreenState.NAVIGATION
        activeTab = NavTab.RUNNING
    }

    // 상태별 분기
    when (screenState) {
        ScreenState.COUNTDOWN -> {
            CountdownScreen(onComplete = handleCountdownComplete)
            return
        }

        ScreenState.ACTIVE_RUNNING -> {
            ActiveRunningScreen(
                onStop = { handleStopOnly() },
                onMenuClick = onMenuClick,
                onFinish = { stats -> handleFinish(stats) }
            )
            return
        }

        ScreenState.NAVIGATION -> Unit
    }

    // ───── 기본 러닝 네비게이션 화면 ─────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // 상단 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Runner’s High",
                fontFamily = RacingSansOne,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onMenuClick() }
            )
        }

        // 오늘의 플랜 + 지도 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // 오늘의 플랜 카드
            TodayPlanCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )

            // 지도 placeholder
            Text(
                text = "이 부분은 네이버 지도 API로\n현재 위치 표시.",
                fontFamily = RacingSansOne,
                fontSize = 28.sp,
                color = Color.Black,
                lineHeight = 34.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 40.dp)
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Black)
        )

        // 하단 네비게이션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            BottomNavItem(
                icon = Icons.Filled.EmojiEvents,
                label = "Level",
                selected = activeTab == NavTab.LEVEL,
                onClick = { activeTab = NavTab.LEVEL }
            )

            BottomNavItem(
                icon = Icons.Filled.FavoriteBorder,
                label = "Running",
                selected = activeTab == NavTab.RUNNING,
                onClick = { activeTab = NavTab.RUNNING }
            )

            BottomNavItem(
                icon = Icons.Filled.PlayCircleOutline,
                label = "Start.",
                selected = activeTab == NavTab.START,
                onClick = {
                    activeTab = NavTab.START
                    handleStartClick()
                }
            )

            BottomNavItem(
                icon = Icons.Filled.Map,
                label = "Course",
                selected = activeTab == NavTab.COURSE,
                onClick = { activeTab = NavTab.COURSE }
            )

            BottomNavItem(
                icon = Icons.Filled.ShowChart,
                label = "Active",
                selected = activeTab == NavTab.STATS,
                onClick = {
                    activeTab = NavTab.STATS
                    onStatsClick()
                }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconTint = if (selected) Color.Black else Color(0xFFCCCCCC)
    val textColor = if (selected) Color.Black else Color(0xFFCCCCCC)

    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = label,
            fontFamily = RacingSansOne,
            fontSize = 14.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun TodayPlanCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu, // 임시 아이콘
                    contentDescription = "오늘의 플랜",
                    tint = Color.Black,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "오늘의 플랜",
                        fontFamily = RacingSansOne,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "7분 페이스로 5KM 완주하기.",
                        fontFamily = RacingSansOne,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
