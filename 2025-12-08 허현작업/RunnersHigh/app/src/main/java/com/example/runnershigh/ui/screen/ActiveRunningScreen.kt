package com.example.runnershigh.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.runnershigh.data.health.HealthConnectManager
import com.example.runnershigh.domain.model.RunningLocationState
import com.example.runnershigh.domain.model.RunningStats
import com.example.runnershigh.ui.RunningViewModel
import com.example.runnershigh.ui.map.rememberMapViewWithLifecycle
import com.example.runnershigh.ui.theme.RacingSansOne
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PolylineOverlay
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ActiveRunningScreen(
    runningViewModel: RunningViewModel,
    onStop: () -> Unit,
    onMenuClick: () -> Unit = {},
    onFinish: (RunningStats) -> Unit = {},
    selectedCoursePath: List<LatLng> = emptyList()
) {
    val context = LocalContext.current
    val healthManager = remember { HealthConnectManager(context) }

    // 비교 오버레이 표시 여부
    var showOverlay by remember { mutableStateOf(false) }

    // 러닝 진행 여부 (true = 진행중, false = 일시정지)
    var isRunning by remember { mutableStateOf(true) }

    // 러닝 시간
    var seconds by remember { mutableIntStateOf(0) }

    // 러닝 데이터 (지금은 일부 더미, 나중에 HealthConnect / 서버 값으로 대체)
    val locationState by runningViewModel.locationState.collectAsState()
    val planGoal by runningViewModel.planGoal.collectAsState()
    val distanceKm = locationState.totalDistanceMeters / 1000.0
    val targetDistanceKm = planGoal.targetDistanceKm.takeIf { it > 0 }

    // 간단 칼로리 추정 (체중 70kg 기준, 1km당 1.036kcal * 체중)
    val userWeightKg = 70.0          // TODO: 유저 프로필에서 가져오기
    val calories = (userWeightKg * distanceKm * 1.036).toInt()

    var bpm by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val heartRates = healthManager.readHeartRates()
            val latestRecord = heartRates.maxByOrNull { it.startTime } ?: return@LaunchedEffect
            val averageBpm = latestRecord.samples.map { it.beatsPerMinute }.average()
            bpm = if (averageBpm.isFinite()) averageBpm.roundToInt() else 0
        } catch (e: Exception) {
            // Health Connect 미연동, 권한 없음 등은 0으로 표시
            bpm = 0
        }
    }

    val currentPaceSecPerKm = if (distanceKm > 0) {
        (seconds / distanceKm).roundToInt()
    } else {
        seconds
    }
    val targetPaceSecPerKm = planGoal.targetPaceSecPerKm ?: 0

    // isRunning 이 true 일 때만 타이머 작동
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            seconds += 1
            // TODO: distanceKm 에 따라 bpm 등을 업데이트 할 수 있음
        }
    }

    val timeText = formatTime(seconds)
    val paceText = formatPace(currentPaceSecPerKm)

    // 뒤로가기: 오버레이가 떠 있을 땐 먼저 오버레이만 닫기
    BackHandler(enabled = showOverlay) {
        showOverlay = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ───────── 왼쪽: 타이틀 + 지도 영역 ─────────
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 24.dp, top = 24.dp, end = 120.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Runner's High.",
                    fontFamily = RacingSansOne,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // 실제 네이버 지도 + 경로 표시
                    ActiveRunningMapSection(
                        locationState = locationState,
                        isRunning = isRunning,
                        onNewLocation = { lat, lng, elevation ->
                            runningViewModel.onNewLocation(lat, lng, elevation)
                        },
                        coursePath = selectedCoursePath,
                        onLongPress = { showOverlay = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ───────── 오른쪽 초록색 정보 패널 ─────────
            RightStatsPanel(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(96.dp)
                    .align(Alignment.CenterEnd),
                timeText = timeText,
                calories = calories,
                paceText = paceText,
                distanceKm = distanceKm,
                targetDistanceKm = targetDistanceKm,
                elevationText = "${locationState.totalElevationGainM.roundToInt()}M",
                bpm = bpm,
                onMenuClick = onMenuClick
            )

            // ───────── 하단 Stop / Pause 버튼 ─────────
            BottomControlButtons(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                isRunning = isRunning,
                onTogglePause = { isRunning = !isRunning },
                onStopLongPress = {
                    // 길게 눌렀을 때 러닝 종료 처리
                    val stats = RunningStats(
                        distanceKm = distanceKm,
                        durationSec = seconds,
                        paceSecPerKm = currentPaceSecPerKm,
                        calories = calories,
                        avgHeartRate = bpm,
                        elevationGainM = locationState.totalElevationGainM.roundToInt(),
                        cadence = 160
                    )
                    onFinish(stats)   // 결과 전달
                    onStop()          // 상위 화면 상태 초기화
                }
            )

            // ───────── 비교 오버레이 ─────────
            if (showOverlay) {
                RunningStatsOverlayRoute(
                    runningViewModel = runningViewModel,
                    distanceKm = distanceKm,
                    elapsedSeconds = seconds,
                    onBack = { showOverlay = false }
                )
            }
        }
    }
}

/* ===== 네이버 지도 + 위치 추적 섹션 ===== */

@Composable
private fun ActiveRunningMapSection(
    locationState: RunningLocationState,
    isRunning: Boolean,
    onNewLocation: (Double, Double, Double?) -> Unit,
    coursePath: List<LatLng>,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var polyline by remember { mutableStateOf<PolylineOverlay?>(null) }
    var coursePolyline by remember { mutableStateOf<PolylineOverlay?>(null) }
    var courseCameraInitialized by remember { mutableStateOf(false) }

    // 위치 콜백 정의 (remember 로 한 번만 생성)
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    onNewLocation(location.latitude, location.longitude, location.altitude)
                }
            }
        }
    }

    LaunchedEffect(coursePath) {
        courseCameraInitialized = false
        if (coursePath.isEmpty()) {
            coursePolyline?.map = null
            coursePolyline = null
        }
    }

    // isRunning 상태에 따라 위치 업데이트 시작/중지
    LaunchedEffect(isRunning) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return@LaunchedEffect

        if (isRunning) {
            // 1초마다 고정 주기로 위치 업데이트 요청
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L
            ).setMinUpdateIntervalMillis(1000L)
                .build()

            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // composable 이 사라질 때 콜백 해제
    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                if (naverMap == null) {
                    view.getMapAsync(object : OnMapReadyCallback {
                        override fun onMapReady(map: NaverMap) {
                            naverMap = map

                            map.uiSettings.isZoomControlEnabled = true
                            map.uiSettings.isLocationButtonEnabled = true
                            map.uiSettings.isZoomGesturesEnabled = true

                            // 지도 길게 누르기 리스너 추가
                            map.setOnMapLongClickListener { _, _ ->
                                onLongPress()
                            }

                            // Polyline 한 번 생성
                            polyline = PolylineOverlay().apply {
                                color = 0xFF00AA00.toInt()
                                width = 16
                                this.map = map
                            }
                        }
                    })
                }

                val map = naverMap
                val line = polyline

                if (map != null) {
                    if (coursePath.size >= 2) {
                        val targetCourseLine = coursePolyline ?: PolylineOverlay().apply {
                            color = 0xFF1976D2.toInt()
                            width = 10
                            this.map = map
                        }

                        targetCourseLine.coords = coursePath
                        coursePolyline = targetCourseLine

                        if (!courseCameraInitialized) {
                            val firstPoint = coursePath.first()
                            map.moveCamera(CameraUpdate.scrollTo(firstPoint))
                            courseCameraInitialized = true
                        }
                    } else {
                        coursePolyline?.map = null
                        coursePolyline = null
                        courseCameraInitialized = false
                    }

                    if (line != null) {
                        val points = locationState.pathPoints

                        // 1) 선 그리기: 좌표가 2개 이상일 때만
                        if (points.size >= 2) {
                            line.coords = points
                        }

                        // 2) 카메라 + 파란 점(현재 위치 오버레이)
                        if (points.isNotEmpty()) {
                            val last = points.last()
                            val cameraUpdate = CameraUpdate.scrollTo(last)
                            map.moveCamera(cameraUpdate)

                            val overlay = map.locationOverlay
                            overlay.isVisible = true
                            overlay.position = last
                        }
                    }
                }
            }
        )
    }
}

/* ===== 패널 / 버튼 / 유틸 함수 ===== */

@Composable
private fun RightStatsPanel(
    modifier: Modifier = Modifier,
    timeText: String,
    calories: Int,
    paceText: String,
    distanceKm: Double,
    targetDistanceKm: Double?,
    elevationText: String,
    bpm: Int,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = modifier.background(Color(0xFF73F212)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onMenuClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = timeText, subtitle = "")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = calories.toString(), subtitle = "Kcal")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = paceText, subtitle = "Pace")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(
            title = String.format(Locale.getDefault(), "%.2f", distanceKm),
            subtitle = "KM"
        )
        Spacer(modifier = Modifier.height(16.dp))

        targetDistanceKm?.let {
            StatItem(
                title = String.format(Locale.getDefault(), "%.1f", it),
                subtitle = "목표 KM"
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        StatItem(title = elevationText, subtitle = "고도")
        Spacer(modifier = Modifier.height(16.dp))

        StatItem(title = bpm.toString(), subtitle = "BPM")
    }
}

@Composable
private fun StatItem(
    title: String,
    subtitle: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontFamily = RacingSansOne,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                fontFamily = RacingSansOne,
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BottomControlButtons(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    onTogglePause: () -> Unit,
    onStopLongPress: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 정지 버튼 (길게 누르기)
        CircleButton(
            icon = Icons.Default.Stop,
            contentDescription = "Stop",
            onClick = null,
            onLongPress = onStopLongPress
        )

        // 일시정지 / 재생 버튼
        CircleButton(
            icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isRunning) "Pause" else "Resume",
            onClick = onTogglePause
        )
    }
}

@Composable
private fun CircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .size(96.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        color = Color(0xFF73F212)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onClick, onLongPress) {
                    detectTapGestures(
                        onLongPress = {
                            onLongPress?.invoke()
                        },
                        onTap = {
                            onClick?.invoke()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

/* ---- 포맷팅 함수 ---- */

private fun formatTime(totalSeconds: Int): String {
    val min = totalSeconds / 60
    val sec = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}

private fun formatPace(secPerKm: Int): String {
    val min = secPerKm / 60
    val sec = secPerKm % 60
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}