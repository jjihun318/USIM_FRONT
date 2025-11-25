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

@Composable
fun GoalSelectionScreen(
    onGoalSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val red = Color(0xFFFF440B)   // ← 여기를 정확히 FF440B로 지정!

    var selectedGoal by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(red)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {

            // 질문 제목
            Text(
                text = "러닝의 목적은?",
                fontFamily = RacingSansOne,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            GoalOptionRow(
                label = "체중 감소.",
                selected = selectedGoal == "체중 감소",
                background = red
            ) {
                selectedGoal = "체중 감소"
                onGoalSelected("체중 감소")
            }

            Spacer(modifier = Modifier.height(16.dp))

            GoalOptionRow(
                label = "마라톤 준비.",
                selected = selectedGoal == "마라톤 준비",
                background = red
            ) {
                selectedGoal = "마라톤 준비"
                onGoalSelected("마라톤 준비")
            }

            Spacer(modifier = Modifier.height(16.dp))

            GoalOptionRow(
                label = "격투기,체력 증진.",
                selected = selectedGoal == "격투기,체력 증진",
                background = red
            ) {
                selectedGoal = "격투기,체력 증진"
                onGoalSelected("격투기,체력 증진")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단 로고
            Text(
                text = "Runner's High.",
                fontFamily = RacingSansOne,
                fontSize = 40.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .align(Alignment.Start)
            )
        }
    }
}

@Composable
private fun GoalOptionRow(
    label: String,
    selected: Boolean,
    background: Color,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onSelect() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,        // 체크박스 박스색
                uncheckedColor = Color.White,
                checkmarkColor = background,       // 체크 mark 색 = FF440B
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = RacingSansOne,
            fontSize = 32.sp,
            color = Color.White
        )
    }
}
