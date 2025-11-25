package com.example.runnershigh.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.runnershigh.ui.RunningViewModel
import com.example.runnershigh.ui.screen.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onGoogleLoginClick: () -> Unit
) {
    // 러닝 관련 화면에서 공유할 ViewModel (Activity 범위)
    val runningViewModel: RunningViewModel = viewModel()

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
                // ✅ 회원가입: 이제 바로 register가 아니라 userInfo로 진입
                onSignUpClick = { navController.navigate("userInfo") },
                onForgotPasswordClick = { navController.navigate("forgotPassword") },
                onLoginSuccess = {
                    // ✅ 로그인 성공 시 바로 러닝 메인 화면으로 이동
                    // TODO: /login_api 호출 후 userId/token 저장하고 이동
                    navController.navigate("running") {
                        // 필요 시 backstack 정책 조정
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
                    // ✅ 회원가입 완료 후 감사합니다 화면으로 이동
                    navController.navigate("thank_you") {
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }

        composable("forgotPassword") {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onResetClick = { email ->
                    // TODO:
                    // 여기에서 실제 "비밀번호 재설정 메일 전송" API 호출 예정
                    // 예: POST /password/reset { email: ... }
                    // 이 함수는 ForgotPasswordScreen 내부에서
                    // "초기화 메일을 전송했습니다." 메시지와 함께 호출됨
                }
            )
        }

        // ✅ 회원가입 플로우의 1단계: 신체 정보
        composable("userInfo") {
            UserInfoScreen(
                onNextClick = { height, weight ->
                    // height / weight 는 필요하면 NavArgument로 넘기거나
                    // 이미 ViewModel에 저장되어 있으니 여기서는 목적 화면으로만 이동
                    navController.navigate("goal")
                }
            )
        }

        // ✅ 회원가입 플로우의 2단계: 목적 선택
        composable("goal") {
            GoalSelectionScreen(
                onGoalSelected = { goalText ->
                    // TODO: 선택된 goalText를 ViewModel에 저장
                    navController.navigate("experience")
                }
            )
        }

        // ✅ 회원가입 플로우의 3단계: 경험 입력 → 이제 register로 이동
        composable("experience") {
            ExperienceScreen(
                onNext = { experience ->
                    // TODO: experience 도 ViewModel에 저장
                    navController.navigate("register")
                }
            )
        }

        // (선택) 감사합니다 화면
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
            RunningScreen(
                navController = navController,
                runningViewModel = runningViewModel,
                onMenuClick = { /* TODO 메뉴 처리 */ },
                onStatsClick = {
                    navController.navigate("runningOverlay")
                },
                onRunningFinish = { _ ->
                    // RunningScreen 에서 finishSession 호출 후 여기로 옴
                    navController.navigate("runningResult")
                }
            )
        }

        // 러닝 중 비교 오버레이 화면
        composable("runningOverlay") {
            RunningStatsOverlayScreen(
                currentPaceSecPerKm = 301,
                targetPaceSecPerKm = 283,
                currentDistanceKm = 6.0,
                targetDistanceKm = 10.0,
                elapsedSeconds = 0,
                onBack = { navController.popBackStack() }
            )
        }

        // 러닝 결과 화면
        composable("runningResult") {
            val stats = runningViewModel.lastStats ?: return@composable

            RunningResultScreen(
                stats = stats,
                targetDistanceKm = 10.0,
                targetPaceSecPerKm = 283,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate("runningFeedback")
                }
            )
        }

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
    }
}
