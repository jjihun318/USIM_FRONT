package com.example.runnershigh.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnershigh.ui.AuthViewModel
import com.example.runnershigh.ui.theme.RacingSansOne

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGoogleLoginClick: () -> Unit,   // 구글 로그인
    viewModel: AuthViewModel

) {
    val uiState by viewModel.loginUiState.collectAsState()
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    // 로그인 성공 시 네비게이션
    LaunchedEffect(uiState.loginCompleted) {
        if (uiState.loginCompleted) {
            onLoginSuccess()
            viewModel.consumeLoginCompletedFlag()
        }
    }

    fun handleLogin() {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            localErrorMessage = "이메일과 비밀번호를 입력해주세요."
            return
        }
        localErrorMessage = null
        viewModel.login()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 Runner's 로고
        Text(
            text = "Runner's",
            fontFamily = RacingSansOne,
            fontWeight = FontWeight.Normal,
            fontSize = 56.sp,
            color = Color(0xFF1E1E1E),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 24.dp)
        )

        // 하단 High. 로고
        Text(
            text = "High.",
            fontFamily = RacingSansOne,
            fontWeight = FontWeight.Normal,
            fontSize = 56.sp,
            color = Color(0xFF1E1E1E),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 32.dp)
        )

        // 뒤로가기 (화살표 대신 텍스트 아이콘으로)
        Text(
            text = "←",
            fontSize = 28.sp,
            color = Color(0xFF1E1E1E),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable { onBack() }
        )

        // 가운데 로그인 폼
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 24.dp)
                .padding(top = 140.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Email
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Email",
                    fontSize = 16.sp,
                    color = Color(0xFF1E1E1E)
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onLoginEmailChange(it) },
                    placeholder = { Text("") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Password
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Password",
                    fontSize = 16.sp,
                    color = Color(0xFF1E1E1E)
                )
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onLoginPasswordChange(it) },
                    placeholder = { Text("") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 에러 메시지 (클라이언트 검증 + 서버/네트워크)
            localErrorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            // Login 버튼
            Button(
                onClick = { handleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2C),
                    contentColor = Color(0xFFF5F5F5)
                ),
                shape = ButtonDefaults.shape
            ) {
                Text(
                    text = if (uiState.isLoading) "Loading..." else "Login",
                    fontSize = 16.sp
                )
            }

            // 비밀번호 찾기
            Text(
                text = "비밀번호 찾기.",
                fontSize = 16.sp,
                color = Color(0xFF1E1E1E),
                modifier = Modifier
                    .clickable { onForgotPasswordClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign in 버튼 (회원가입)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onSignUpClick() },
                    modifier = Modifier
                        .width(256.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C2C),
                        contentColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Text(text = "Sign in", fontSize = 16.sp)
                }
            }

            // Google 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = { onGoogleLoginClick() },
                    modifier = Modifier
                        .width(96.dp)
                        .height(48.dp),
                ) {
                    Text(text = "G", fontSize = 20.sp)
                }
            }
        }
    }
}
