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
import com.example.runnershigh.ui.screen.course.AddCourseScreen
import com.example.runnershigh.ui.screen.course.CourseDetailScreen
import com.example.runnershigh.ui.screen.course.MyCourseScreen
import com.example.runnershigh.ui.screen.course.PopularCourseScreen
import com.example.runnershigh.ui.screen.course.RecentCourseViewModel
import com.example.runnershigh.ui.screen.course.RegisterCourseScreen
import com.example.runnershigh.ui.screen.course.RunningCourseScreen
import com.example.runnershigh.ui.screen.course.toRecentRunCardData
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
    val bodyUiState by authViewModel.bodyUiState.collectAsState()

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
                onGoogleLoginClick = onGoogleLoginClick,
                viewModel = authViewModel
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

        composable("course/main") {
            val userUuid = authViewModel.userUuid ?: ""
            val recentCourseViewModel: RecentCourseViewModel = viewModel()
            val recentUiState by recentCourseViewModel.uiState.collectAsState()

            LaunchedEffect(userUuid) {
                if (userUuid.isNotBlank()) {
                    recentCourseViewModel.loadRecentActivities(userUuid)
                }
            }

            RunningCourseScreen(
                onBackClick = { navController.popBackStack() },
                onLocationCourseClick = { navController.navigate("course/detail") },
                onPopularCourseClick = { navController.navigate("course/popular") },
                onMyCourseClick = { navController.navigate("course/my") },
                runningViewModel = runningViewModel,
                recentActivities = recentUiState.recentActivities,
                isLoading = recentUiState.isLoading,
                errorMessage = recentUiState.errorMessage
            )
        }

        composable("course/detail") {
            CourseDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("course/popular") {
            PopularCourseScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("course/my") {
            val userUuid = authViewModel.userUuid ?: ""
            val courses by runningViewModel.userCourses.collectAsState()
            val isLoading by runningViewModel.coursesLoading.collectAsState()
            val courseError by runningViewModel.courseError.collectAsState()

            LaunchedEffect(userUuid) {
                if (userUuid.isNotBlank()) {
                    runningViewModel.loadUserCourses(userUuid)
                }
            }

            MyCourseScreen(
                { navController.popBackStack() },
                { navController.navigate("course/my/register_recent") },
                courses,
                isLoading,
                courseError,
                { course ->
                    course.courseId?.let { courseId ->
                        runningViewModel.deleteCourse(userUuid, courseId)
                    }
                },
                { course ->
                    runningViewModel.selectCourse(course)
                    navController.navigate("running") {
                        popUpTo("running") { inclusive = false }
                    }
                }
            )
        }

        composable("course/my/register_recent") {
            val userUuid = authViewModel.userUuid ?: ""
            val recentCourseViewModel: RecentCourseViewModel = viewModel()
            val recentUiState by recentCourseViewModel.uiState.collectAsState()

            LaunchedEffect(userUuid) {
                if (userUuid.isNotBlank()) {
                    recentCourseViewModel.loadRecentActivities(userUuid)
                }
            }

            AddCourseScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterClick = { sessionId, date, location, distance, time ->
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("selectedSessionId", sessionId)
                        set("selectedDate", date)
                        set("selectedLocation", location)
                        set("selectedDistance", distance)
                        set("selectedTime", time)
                    }
                    navController.navigate("course/register_form")
                },
                recentRuns = recentUiState.recentActivities.map { it.toRecentRunCardData() },
                isLoading = recentUiState.isLoading,
                errorMessage = recentUiState.errorMessage
            )
        }

        composable("course/register_form") {
            val sessionId = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selectedSessionId")
                ?: ""
            val date = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selectedDate")
                ?: ""
            val location = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selectedLocation")
                ?: ""
            val distance = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selectedDistance")
                ?: ""
            val time = navController.previousBackStackEntry?.savedStateHandle?.get<String>("selectedTime")
                ?: ""
            val userUuid = authViewModel.userUuid ?: ""

            RegisterCourseScreen(
                date = date,
                location = location,
                distance = distance,
                time = time,
                sessionId = sessionId,
                onBackClick = { navController.popBackStack() },
                runningViewModel = runningViewModel,
                userUuid = userUuid,
                onRegisterSuccess = {
                    navController.navigate("course/my") {
                        popUpTo("running") { inclusive = false }
                    }
                }
            )
        }

        // (예전) 러닝 중 비교 오버레이 화면 – 필요하면 계속 사용
        composable("runningOverlay") {
            val locationState by runningViewModel.locationState.collectAsState()
            RunningStatsOverlayRoute(
                runningViewModel = runningViewModel,
                distanceKm = locationState.totalDistanceMeters / 1000.0,
                elapsedSeconds = 0,
                onBack = { navController.popBackStack() }
            )
        }

        // 러닝 결과 화면
        composable("runningResult") {
            val userUuid = authViewModel.userUuid ?: ""

            // 화면 진입 시 한 번만 결과 불러오기
            LaunchedEffect(userUuid) {
                if (userUuid.isNotBlank()) {
                    runningViewModel.updateUserUuid(userUuid)
                    runningViewModel.loadRunningResult()
                }
            }

            // Flow 를 Compose state 로 변환
            val result by runningViewModel.resultState
                .collectAsState(initial = null as SessionResultResponse?)
            val compare by runningViewModel.compareState.collectAsState()
            val planGoal by runningViewModel.planGoal.collectAsState()
            result?.let { res ->
                val stats = res.toRunningStats()
                val targetDistance = planGoal.targetDistanceKm.takeIf { it > 0 }
                    ?: compare?.targetDistanceKm?.takeIf { it > 0 }
                    ?: stats.distanceKm
                val targetPaceSec = planGoal.targetPaceSecPerKm
                    ?: compare?.targetPaceSec
                    ?: 0
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
                    titleLabel = titleLabel,
                    badgeAcquired = res.badgeAcquired,
                    gainedExperience = res.gainedExperience
                )
            }
        }

        // 러닝 피드백 화면
        composable("runningFeedback") {
            val userUuid = authViewModel.userUuid ?: ""
            LaunchedEffect(userUuid) {
                if (userUuid.isNotBlank()) {
                    runningViewModel.loadSubmittedFeedback(userUuid)
                }
            }
            val previousFeedback by runningViewModel.submittedFeedback.collectAsState()
            RunningFeedbackScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { feedback ->
                    runningViewModel.submitFeedback(userUuid, feedback)
                    navController.popBackStack("running", inclusive = false)
                },
                previousFeedback = previousFeedback,
                onCreateCourseClick = {
                    navController.navigate("course/register_form")
                }
            )
        }
        composable("level") {
            val userUuid = authViewModel.userUuid ?: ""
            ActiveScreen(
                navController = navController,
                userUuid = userUuid,
                userHeightCm = bodyUiState.height.toDoubleOrNull(),
                userWeightKg = bodyUiState.weight.toDoubleOrNull()
            )
        }
        // 🔷 활동(통계) 메인 화면
        composable("active") {
            val userUuid = authViewModel.userUuid ?: ""
            ActiveScreen(
                navController = navController,
                userUuid = userUuid,
                userHeightCm = bodyUiState.height.toDoubleOrNull(),
                userWeightKg = bodyUiState.weight.toDoubleOrNull()
            )
        }

        // 🔷 컨디션 상세 화면
        composable("active/condition") {
            val userUuid = authViewModel.userUuid ?: ""
            ConditionDetailScreen(
                onBackClick = { navController.popBackStack() },
                userUuid = userUuid
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
