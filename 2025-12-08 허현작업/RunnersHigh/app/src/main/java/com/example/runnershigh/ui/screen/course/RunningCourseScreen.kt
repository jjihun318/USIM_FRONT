package com.example.runnershigh.ui.screen.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnershigh.ui.screen.course.components.CourseCard
import com.example.runnershigh.ui.screen.course.components.GuideCard
import com.example.runnershigh.ui.screen.course.components.RecentCourseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningCourseScreen(
    onBackClick: () -> Unit,
    onLocationCourseClick: () -> Unit,
    onPopularCourseClick: () -> Unit,
    onMyCourseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("러닝 코스", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "현재 위치",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "대구광역시 수성구",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }


        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CourseCard(
                icon = "navigation",
                title = "위치 기반 맞춤형 코스",
                description = "현재 위치에 기반한 최적화 코스를 추천합니다!",
                badges = listOf("#개인 맞춤형", "#실시간 분석"),
                color = Color(0xFFB8E6B8),
                onClick = onLocationCourseClick
            )

            CourseCard(
                icon = "trending",
                title = "인기 러닝 코스",
                description = "다른 러너들의 추천 코스를 골라보세요!",
                badges = listOf("#주간 TOP 100", "#후기"),
                color = Color(0xFFD4F1D4),
                onClick = onPopularCourseClick
            )

            CourseCard(
                icon = "map",
                title = "나만의 코스 관리",
                description = "코스 추가, 삭제, 선택하기",
                badges = listOf("#커스텀", "#공유 가능"),
                color = Color(0xFFE8F5E8),
                onClick = onMyCourseClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GuideCard()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "최근에 달린 코스",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecentCourseItem("수성동 둘레길", "4.2km", "|   공원", Color(0xFFFFF4E6))
            RecentCourseItem("범어천 러닝로드", "6.8km", "|   하천", Color(0xFFE8F5E8))
            RecentCourseItem("앞산 순환로", "8.5km", "|   산책로", Color(0xFFFFE6E6))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
    }


