package com.example.runnershigh.ui.screen.level

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnershigh.R // 리소스 ID가 있는 패키지로 변경하세요.
import com.example.runnershigh.ui.theme.RunnersHighTheme

// XML의 #333333, #99BC62, #E5F3CC 색상에 대응하는 Compose Color 정의
val ColorPrimaryText = Color(0xFF333333)
val ColorProgressBarTint = Color(0xFF333333)
val ColorProgressBackground = Color(0xFF99BC62)
val ColorCardBackground = Color(0xFFE5F3CC)
val ColorSecondaryText = Color(0xFF888888)
val ColorConditionGood = Color(0xFF99BC62) // #99BC62

@Composable
fun LvScreen(
    onLoginClick: () -> Unit,
    // 이 외에 필요한 이벤트 핸들러 추가 가능 (예: onMenuClick, onLevelCardClick)
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // ScrollView 역할
                .padding(16.dp)
        ) {
            // 1. 헤더 영역: "Runner's High" + 메뉴 아이콘
            AppHeader()

            Spacer(modifier = Modifier.height(8.dp)) // XML의 margin-bottom 24dp와 Padding 조합

            // 2. 레벨/EXP 카드 (CardView 역할)
            LevelAndExpCard(onLevelCardClick = { /* TODO: 상세 화면 이동 */ })

            // 3. Training Mission 헤더
            MissionHeader(completedCount = 1, totalCount = 3)

            // 4. Mission 목록 (include layout) - 임시 Composable로 대체
            MissionList()

            // 5. Condition Level 섹션 헤더
            ConditionLevelHeader()

            // 6. Condition Level 카드
            ConditionLevelCard()
        }
    }
}

// 1. 헤더 영역: "Runner's High" + 메뉴 아이콘
@Composable
fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Runner's High",
            fontSize = 24.sp,
            color = ColorPrimaryText,
            fontWeight = FontWeight.Bold
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_menu), // ic_menu 리소스 필요
            contentDescription = "메뉴",
            tint = ColorPrimaryText,
            modifier = Modifier.size(30.dp)
        )
    }
}

// 2. 레벨/EXP 카드
@Composable
fun LevelAndExpCard(onLevelCardClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBackground), // #E5F3CC
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .clickable { onLevelCardClick() } // clickable, focusable 역할
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Level 텍스트
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Level 1",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryText
                )
            }

            // EXP 텍스트 및 프로그레스 값 (458/1000)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryText
                )
                Text(
                    text = "458/1000",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryText
                )
            }

            // ProgressBar
            LinearProgressIndicator(
                progress = { 0.45f }, // progress="45", max="100" 에 대응
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .padding(bottom = 12.dp),
                color = ColorProgressBarTint, // progressTint="#333333"
                trackColor = ColorProgressBackground // progressBackgroundTint="#99BC62"
            )

            // 남은 EXP 텍스트
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "-550 EXP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryText
                )
            }
        }
    }
}

// 3. Training Mission 헤더
@Composable
fun MissionHeader(completedCount: Int, totalCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Training Mission",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPrimaryText
        )
        Text(
            text = "$completedCount/$totalCount",
            fontSize = 14.sp,
            color = ColorSecondaryText // #888888
        )
    }
}

// 4. Mission 목록 (XML <include> 대체)
@Composable
fun MissionList() {
    Column {
        // <include layout="@layout/level_mission_3" />
        MissionItemPlaceholder(text = "미션 3 Placeholder")
        // <include layout="@layout/level_mission_2" />
        MissionItemPlaceholder(text = "미션 2 Placeholder")
        // <include layout="@layout/level_mission_1" />
        MissionItemPlaceholder(text = "미션 1 Placeholder")
    }
}

@Composable
fun MissionItemPlaceholder(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = ColorPrimaryText
        )
    }
}


// 5. Condition Level 섹션 헤더
@Composable
fun ConditionLevelHeader() {
    Text(
        text = "Condition Level",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = ColorPrimaryText,
        modifier = Modifier
            .padding(top = 24.dp, bottom = 12.dp)
            .fillMaxWidth()
    )
}


// 6. Condition Level 카드
@Composable
fun ConditionLevelCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBackground), // #E5F3CC
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_condition), // ic_condition 리소스 필요
                    contentDescription = "컨디션 그래프",
                    tint = ColorPrimaryText,
                    modifier = Modifier.size(40.dp)
                )

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "현재 컨디션 지수",
                        fontSize = 14.sp,
                        color = ColorPrimaryText
                    )
                    Text(
                        text = "좋음",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorConditionGood // #99BC62
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LvScreenPreview() {
    RunnersHighTheme {
        LvScreen(onLoginClick = {})
    }
}