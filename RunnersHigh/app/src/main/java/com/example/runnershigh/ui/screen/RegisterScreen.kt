package com.example.runnershigh.ui.screen

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runnershigh.ui.AuthViewModel
import com.example.runnershigh.ui.theme.RacingSansOne

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    // 회원가입 성공 시 다음 화면으로 네비게이션 하는 콜백
    onSignupSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.signupUiState.collectAsState()

    // 동의 체크박스는 서버로 안 보내는 값이니까 로컬 상태로만 관리
    var agreed by remember { mutableStateOf(false) }

    // 클라이언트(입력값) 검증 에러
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    /**
     * 회원가입 성공 플래그를 감지해서
     * 외부(NavGraph)로 성공 콜백 보내기
     */
    LaunchedEffect(uiState.signupCompleted) {
        if (uiState.signupCompleted) {
            onSignupSuccess()
            viewModel.consumeSignupCompletedFlag()
        }
    }

    fun validateAndRegister() {
        val email = uiState.email
        val password = uiState.password
        val nickname = uiState.username

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            localErrorMessage = "유효한 이메일 주소를 입력해주세요."
            return
        }

        if (uiState.isEmailAvailable != true) {
            localErrorMessage = "이메일 중복을 먼저 확인해주세요."
            return
        }

        if (password.length < 6) {
            localErrorMessage = "비밀번호는 최소 6자 이상이어야 합니다."
            return
        }

        if (nickname.isBlank()) {
            localErrorMessage = "닉네임을 입력해주세요."
            return
        }

        // ✅ username 체크 추가
        if (uiState.isUsernameAvailable != true) {
            localErrorMessage = "닉네임 중복을 먼저 확인해주세요."
            return
        }

        if (!agreed) {
            localErrorMessage = "건강 정보 수집에 동의해야 회원가입이 가능합니다."
            return
        }

        localErrorMessage = null
        viewModel.signup()
    }

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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    modifier = Modifier
                        .weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    placeholder = { Text(text = "") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.checkEmail() },
                    enabled = !uiState.isCheckingEmail,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (uiState.isCheckingEmail) "확인중..." else "중복 확인",
                        fontSize = 12.sp
                    )
                }
            }
            uiState.emailCheckMessage?.let { msg ->
                Text(
                    text = msg,
                    color = if (uiState.isEmailAvailable == true) Color(0xFF4CAF50) else Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Password
            Text(
                text = "Password",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text(text = "") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nickname
            Text(
                text = "Nickname",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text(text = "") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.checkUsername() },
                    enabled = !uiState.isCheckingUsername,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (uiState.isCheckingUsername) "확인중..." else "중복 확인",
                        fontSize = 12.sp
                    )
                }
            }

            uiState.usernameCheckMessage?.let { msg ->
                Text(
                    text = msg,
                    color = if (uiState.isUsernameAvailable == true) Color(0xFF4CAF50) else Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🎯 클라이언트 검증 에러
            localErrorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 🎯 서버/네트워크 에러 (ViewModel 쪽)
            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 🎯 성공 메시지 (원하면 UI에서 안 보여줘도 됨)
            uiState.successMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Register 버튼
            Button(
                onClick = { validateAndRegister() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    Text("Processing...")
                } else {
                    Text(text = "Register")
                }
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
