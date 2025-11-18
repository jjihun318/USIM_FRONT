package com.example.runnershigh.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnershigh.ui.theme.RacingSansOne

@Composable
fun RunningScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 앱 로고 + 메뉴
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
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }

            // 중앙: 지도/러닝 정보 영역 (지금은 설명 텍스트만)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "이 부분은 네이버 지도 API로\n현재 위치 표시.",
                    fontFamily = RacingSansOne,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    color = Color.Black
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.Black)
            )

            // 하단 네비게이션 바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Level 아이콘
                BottomNavItem(
                    icon = Icons.Filled.EmojiEvents,
                    label = "Level",
                    selected = false
                )
                // Running (현재 선택됨)
                BottomNavItem(
                    icon = Icons.Filled.FavoriteBorder,
                    label = "Running",
                    selected = true
                )
                // Start
                BottomNavItem(
                    icon = Icons.Filled.PlayCircleOutline,
                    label = "Start.",
                    selected = false
                )
                // Course
                BottomNavItem(
                    icon = Icons.Filled.Map,
                    label = "Course",
                    selected = false
                )
                // Level 그래프
                BottomNavItem(
                    icon = Icons.Filled.ShowChart,
                    label = "Level",
                    selected = false
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean
) {
    val iconTint = if (selected) Color.Black else Color(0xFFCCCCCC)
    val textColor = if (selected) Color.Black else Color(0xFFCCCCCC)

    Column(
        modifier = Modifier.width(72.dp),
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
