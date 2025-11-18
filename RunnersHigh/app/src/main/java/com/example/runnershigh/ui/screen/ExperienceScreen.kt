package com.example.runnershigh.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnershigh.ui.theme.RacingSansOne

enum class Experience {
    Beginner,   // 전혀 없음
    Experienced, // 경험 있음
    Competitor   // 대회 참가 경력자
}

@Composable
fun ExperienceScreen(
    onNext: (String) -> Unit
) {
    val backgroundColor = Color(0xFFF29C12)

    var selected by remember { mutableStateOf<Experience?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {

            // 제목
            Text(
                text = "러닝을 해본 적이\n있습니까?",
                color = Color.White,
                fontFamily = RacingSansOne,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                lineHeight = 46.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            ExperienceOption(
                label = "전혀 없음.",
                selected = selected == Experience.Beginner,
                backgroundColor = backgroundColor,
            ) {
                selected = Experience.Beginner
                onNext("전혀 없음")
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExperienceOption(
                label = "경험 있음.",
                selected = selected == Experience.Experienced,
                backgroundColor = backgroundColor,
            ) {
                selected = Experience.Experienced
                onNext("경험 있음")
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExperienceOption(
                label = "대회 참가 경력자.",
                selected = selected == Experience.Competitor,
                backgroundColor = backgroundColor,
            ) {
                selected = Experience.Competitor
                onNext("대회 참가 경력자")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 아래 Runner's High.
            Text(
                text = "Runner's High.",
                color = Color.White,
                fontFamily = RacingSansOne,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun ExperienceOption(
    label: String,
    selected: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,      // 체크된 박스 배경은 흰색
                uncheckedColor = Color.White,    // 체크 안 된 박스 테두리 색도 흰색
                checkmarkColor = backgroundColor // ✔ 색을 배경색과 동일하게
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = Color.White,
            fontFamily = RacingSansOne,
            fontSize = 28.sp
        )
    }
}
