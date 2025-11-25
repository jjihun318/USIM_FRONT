package com.example.runnershigh.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.runnershigh.ui.theme.RacingSansOne
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color


@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterClick: (email: String, password: String, agreed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // 뒤로가기 아이콘 (오른쪽 위)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Runner's",
                fontFamily = RacingSansOne,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email
            Text(
                text = "Email",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                placeholder = { Text(text = "") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            Text(
                text = "Password",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text(text = "") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 동의 체크박스
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "동의",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "건강 정보 수집",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register 버튼
            Button(
                onClick = { onRegisterClick(email, password, agreed) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,      // 버튼 배경색
                    contentColor = Color.White),
                    modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Register")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // High.
            Text(
                text = "High.",
                fontFamily = RacingSansOne,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
