package com.example.runnershigh.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.runnershigh.ui.screen.LoginScreen
import com.example.runnershigh.ui.screen.MainScreen
import com.example.runnershigh.ui.screen.RegisterScreen
import com.example.runnershigh.ui.screen.ForgotPasswordScreen
import com.example.runnershigh.ui.screen.UserInfoScreen
import com.example.runnershigh.ui.screen.GoalSelectionScreen
import com.example.runnershigh.ui.screen.ExperienceScreen
import com.example.runnershigh.ui.screen.ThankYouScreen
import com.example.runnershigh.ui.screen.RunningScreen



@Composable
fun AppNavGraph(
    navController: NavHostController,
    onGoogleLoginClick: () -> Unit
) {
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
                onSignUpClick = {  navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("forgotPassword") },
                onLoginSuccess = { navController.navigate("userInfo") },
                onGoogleLoginClick = onGoogleLoginClick  // 이건 이제 구글 로그인 전용
            )
        }

        composable("register") {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterClick = { email, password, agreed ->
                    // TODO: Firebase Auth 회원가입 로직 연결
                }
            )
        }


        composable("forgotPassword") {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onResetClick = { email ->
                    /* TODO: Firebase Auth 비밀번호 리셋 로직 */
                }
            )
        }

        composable(route = "userInfo") {
            UserInfoScreen(
                onNextClick = { height, weight ->
                    navController.navigate("goal")

                }
            )
        }

        composable(route = "goal") {
            GoalSelectionScreen(
                onGoalSelected = { goalText ->
                    // TODO: goalText를 ViewModel 등에 저장하고
                    // 다음 화면으로 네비게이션
                    navController.navigate("experience")
                }
            )
        }

        composable(route = "experience") {
            ExperienceScreen(
                onNext = { experience ->
                    navController.navigate("thank_you")

                }
            )
        }

        composable(route = "thank_you") {
            ThankYouScreen(
                onComplete = {
                    navController.navigate("running") {
                        // 뒤로가기로 다시 땡큐 화면 안 돌아오게
                        popUpTo("thank_you") { inclusive = true }
                    }
                }
            )
        }
        composable(route = "running") {
            RunningScreen()
        }

    }
}