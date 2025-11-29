package com.example.runnershigh.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runnershigh.ui.AuthViewModel
import com.example.runnershigh.ui.RunningViewModel

import com.example.runnershigh.ui.screen.*
import com.example.runnershigh.domain.model.*
import com.example.runnershigh.util.parsePaceToSeconds
import com.example.runnershigh.data.remote.dto.*
import com.example.runnershigh.ui.map.NaverMapTestScreen
import com.example.runnershigh.ui.screen.active.ActiveScreen
import com.example.runnershigh.ui.screen.active.ConditionDetailScreen
import androidx.compose.ui.unit.dp
@Composable
fun AppNavGraph(
    navController: NavHostController,
    onGoogleLoginClick: () -> Unit
) {
    // 러닝 관련 화면에서 공유할 ViewModel (Activity 범위)
    val runningViewModel: RunningViewModel = viewModel()
    // 회원가입 / 로그인 플로우용 ViewModel
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onLoginClick = { navController.navigate("login") }
            )
        }

        composable("login") {
            LoginScreen(
                onBack = { navController.popBackStack() },
                // 회원가입: userInfo로 진입
                onSignUpClick = { navController.navigate("userInfo") },
                onForgotPasswordClick = { navController.navigate("forgotPassword") },
                onLoginSuccess = {
                    // 로그인 성공 시 러닝 메인 화면으로 이동
                    // TODO: /login_api 호출 후 userId/token 저장
                    navController.navigate("running") {
                        popUpTo("main") { inclusive = false }
                    }
                },
                onGoogleLoginClick = onGoogleLoginClick
            )
        }

        // 회원가입 최종 단계 화면
        composable("register") {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onSignupSuccess = {
                    // 회원가입 완료 후 감사합니다 화면으로 이동
                    navController.navigate("thank_you") {
                        popUpTo("main") { inclusive = false }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable("forgotPassword") {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onResetClick = { email ->
                    // TODO: 비밀번호 재설정 메일 전송 API
                }
            )
        }

        // 1단계: 신체 정보
        composable("userInfo") {
            UserInfoScreen(
                onNextClick = { height, weight ->
                    // height / weight 는 ViewModel 에 이미 저장됨
                    navController.navigate("goal")
                },
                viewModel = authViewModel
            )
        }

        // 2단계: 목적 선택
        composable("goal") {
            GoalSelectionScreen(
                onGoalSelected = { goalText ->
                    authViewModel.onPurposeSelected(goalText)
                    navController.navigate("experience")
                },
                authViewModel = authViewModel
            )
        }

        // 3단계: 경험 입력 → register 로 이동
        composable("experience") {
            ExperienceScreen(
                onNext = { experience ->
                    // TODO: experience 도 ViewModel에 저장
                    navController.navigate("register")
                },
                viewModel = authViewModel
            )
        }

        // 감사합니다 화면
        composable("thank_you") {
            ThankYouScreen(
                onComplete = {
                    navController.navigate("running") {
                        popUpTo("thank_you") { inclusive = true }
                    }
                }
            )
        }

        // 러닝 메인 화면 (탭 + Start 버튼)
        composable("running") {

            val userUuid = authViewModel.userUuid ?: ""
            RunningScreen(
                navController = navController,
                runningViewModel = runningViewModel,
                userUuid = userUuid,
                onMenuClick = { /* TODO 메뉴 처리 */ },
                onStatsClick = {
                    // 🔥 하단 Active 탭 → 활동(통계) 화면으로 이동
                    navController.navigate("active")
                },
                onRunningFinish = {
                    // RunningScreen 에서 finishSession 호출 후 여기로 옴
                    navController.navigate("runningResult")
                }
            )
        }

        // (예전) 러닝 중 비교 오버레이 화면 – 필요하면 계속 사용
        composable("runningOverlay") {
            RunningStatsOverlayRoute(
                runningViewModel = runningViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 러닝 결과 화면
        composable("runningResult") {

            // 화면 진입 시 한 번만 결과 불러오기
            LaunchedEffect(Unit) {
                runningViewModel.loadRunningResult()
            }

            // Flow 를 Compose state 로 변환
            val result by runningViewModel.resultState
                .collectAsState(initial = null as SessionResultResponse?)

            result?.let { res ->
                val stats = res.toRunningStats()
                val targetDistance = stats.distanceKm          // 일단 실제 달린 거리 기준
                val targetPaceSec = parsePaceToSeconds(res.targetPace)
                val dateLabel = formatDateLabel(res.date)
                val titleLabel = res.courseName

                RunningResultScreen(
                    stats = stats,
                    targetDistanceKm = targetDistance,
                    targetPaceSecPerKm = targetPaceSec,
                    onBack = { navController.popBackStack() },
                    onNext = {
                        // 러닝 피드백 화면으로 이동
                        navController.navigate("runningFeedback")
                    },
                    dateTimeLabel = dateLabel,
                    titleLabel = titleLabel
                )
            }
        }

        // 러닝 피드백 화면
        composable("runningFeedback") {
            RunningFeedbackScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { feedback ->
                    // TODO: ViewModel 통해 서버에 피드백 전송
                    // runningViewModel.submitFeedback(sessionId, feedback)
                    navController.popBackStack("running", inclusive = false)
                }
            )
        }

        // 🔷 활동(통계) 메인 화면
        composable("active") {
            ActiveScreen(navController = navController)
        }

        // 🔷 컨디션 상세 화면
        composable("active/condition") {
            ConditionDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 🔷 목표 상세 화면 – 아직 UI 없으니 임시 Text로 대체
        composable("active/goal") {
            Text(
                text = "목표 상세 화면은 준비 중입니다.",
                modifier = Modifier.padding(16.dp)
            )
        }

        // 네이버 지도 테스트용 화면
        composable("naver_map_test") {
            NaverMapTestScreen()
        }
    }
}
