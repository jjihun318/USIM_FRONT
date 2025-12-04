package com.example.coursecreater.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuideCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF4A7C4E)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "러닝 코스 이용 가이드",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "추천 코스는 현재 위치 기반으로 제공됩니다",
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            Text(
                text = "인기 코스는 주간 단위로 업데이트 됩니다",
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            Text(
                text = "나만의 코스를 만들어 공유할 수 있습니다",
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
