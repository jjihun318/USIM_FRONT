package com.example.runnershigh.ui.screen.mate

import android.util.Log
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.runnershigh.R
import androidx.compose.ui.zIndex
import com.example.runnershigh.ui.RunningViewModel
import com.example.runnershigh.ui.map.RunningMapSection
import com.example.runnershigh.ui.theme.RacingSansOne
import com.naver.maps.geometry.LatLng
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.runnershigh.data.remote.dto.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.ArrowDropDown

import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

private data class RunnerPresenceChip(
    val label: String,
    val alignX: Float,
    val alignY: Float
)

private data class MatchingConfigCard(
    val iconLabel: String,
    val title: String,
    val description: String,
    val value: String,
    val cardColor: Color,
    val textColor: Color
)

private enum class RunningMateEntryMode { CREATE, FIND }
enum class MatchParticipationType { CREATED, JOINED }

private data class RunningMatchPost(
    val title: String,
    val summary: String,
    val meta: String,
    val accentColor: Color,
    val currentMembers: Int,
    val maxMembers: Int,
    val startHour: Int,
    val startMinute: Int
)

data class ActiveRunningMateReservation(
    val roomId: String,
    val title: String,
    val participationType: MatchParticipationType,
    val currentMembers: Int,
    val maxMembers: Int,
    val startHour: Int,
    val startMinute: Int
)
public data class SearchMode( var search: Boolean )

@Composable
fun RunningMateLoadingScreen(
    modifier: Modifier = Modifier
) {
    val mateOrange = Color(0xFFFF7A1A)

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mate_loading_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "running mate",
                fontFamily = RacingSansOne,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                lineHeight = 44.sp,
                color = mateOrange,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            CircularProgressIndicator(
                color = mateOrange,
                trackColor = Color.White.copy(alpha = 0.28f),
                strokeWidth = 5.dp,
                modifier = Modifier.size(46.dp)
            )
        }
    }
}

//러닝 매칭 만들기
@Composable
fun RunningMateMapScreen(
    onClose: () -> Unit,
    coursePath: List<LatLng>,
    initialReservation: ActiveRunningMateReservation? = null,
    onReservationChanged: (ActiveRunningMateReservation) -> Unit = {},
    viewModel: RunningViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),

    userUuid: String = ""
) {
    var showMatchingSetupScreen by rememberSaveable { mutableStateOf(false) }
    var entryMode by rememberSaveable { mutableStateOf(RunningMateEntryMode.CREATE) }
    var activeReservation by remember(initialReservation) { mutableStateOf(initialReservation) }
    val mode = SearchMode(search = false)
    var searchMode by remember { mutableStateOf(SearchMode(search = false)) }
    LaunchedEffect(initialReservation) {
        viewModel.activeReservation = initialReservation
    }
    if (showMatchingSetupScreen) {
        RunningMateMatchingSetupScreen(
            viewModel = viewModel,
            userUuid = userUuid,
            mode = entryMode,
            onClose = onClose,
            onBack = { showMatchingSetupScreen = false },
            onMatchConfirmed = { reservation ->
                viewModel.activeReservation = reservation
                onReservationChanged(reservation)
                showMatchingSetupScreen = false
            },
            coursePath = coursePath
        )
        return
    }

    val paletteBackground = Color(0xFFF7F3E6)
    val accentYellow = Color(0xFFFFD84D)
    val deepNavy = Color(0xFF1F2337)
    val chipBlue = Color(0xFF1780E6)

    /*val activeRunnerChips = remember {
        listOf(
            RunnerPresenceChip("24명", -0.72f, -0.34f),
            RunnerPresenceChip("8명", -0.12f, -0.54f),
            RunnerPresenceChip("15명", 0.56f, -0.20f),
            RunnerPresenceChip("6명", -0.46f, 0.06f),
            RunnerPresenceChip("11명", 0.18f, 0.22f),
            RunnerPresenceChip("4명", 0.72f, 0.44f)
        )
    }*/

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(paletteBackground)
    ) {
        RunningMapSection(
            modifier = Modifier.matchParentSize(),
            coursePath = coursePath
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = "running mate",
                fontFamily = RacingSansOne,
                fontSize = 30.sp,
                color = deepNavy
            )

            Spacer(modifier = Modifier.size(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MateActionButton(
                    modifier = Modifier.weight(1f),
                    text = "러닝 매칭 만들기",
                    icon = Icons.Filled.AddCircleOutline,
                    backgroundColor = accentYellow,
                    contentColor = deepNavy,
                    onClick = {
                        entryMode = RunningMateEntryMode.CREATE
                        showMatchingSetupScreen = true
                        searchMode = searchMode.copy(search = false)
                    }
                )
                MateActionButton(
                    modifier = Modifier.weight(1f),
                    text = "매칭 찾기 시작",
                    icon = Icons.Filled.Search,
                    backgroundColor = Color.White,
                    contentColor = deepNavy,
                    onClick = {
                        entryMode = RunningMateEntryMode.FIND
                        showMatchingSetupScreen = true
                        searchMode = searchMode.copy(search = true)
                    }
                )
            }

            viewModel.activeReservation?.let { reservation ->
                Spacer(modifier = Modifier.height(10.dp))
                ActiveReservationCard(
                    reservation = reservation,
                    viewModel = viewModel,
                    currentUserId = userUuid
                )
            }
        }

        Surface(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 18.dp)
                .zIndex(20f),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "닫기",
                tint = Color(0xFFE83838),
                modifier = Modifier.padding(10.dp)
            )
        }

       /* activeRunnerChips.forEach { chip ->
            RunnerPresenceBadge(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (chip.alignX * 150).dp, y = (chip.alignY * 260).dp),
                label = chip.label,
                iconTint = chipBlue
            )
        }*/

        /*Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = chipBlue
                )
                Text(
                    text = "현재 주변 러너 68명 활동 중",
                    color = deepNavy,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }*/
    }
}

@Composable
private fun RunningMateMatchingSetupScreen( //if (mode == RunningMateEntryMode.CREATE)
    viewModel: RunningViewModel, // 👈 추가
    userUuid: String,
    mode: RunningMateEntryMode,
    onClose: () -> Unit,
    onBack: () -> Unit,
    onMatchConfirmed: (ActiveRunningMateReservation) -> Unit,
    coursePath: List<LatLng>
) {
    val context = LocalContext.current
    var selectedCardIndex by remember { mutableIntStateOf(0) }
    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var pendingStartLocation by remember { mutableStateOf<LatLng?>(null) }
    var isStartLocationPickerMode by remember { mutableStateOf(false) }
    var locationSearchQuery by remember { mutableStateOf("") }

    var distanceKm by remember { mutableIntStateOf(5) }
    var runnerCount by remember { mutableIntStateOf(2) }
    var showDistancePicker by remember { mutableStateOf(false) }
    var showRunnerCountPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val levelModes = listOf("동일 레벨을 원해요", "멘토를 원해요", "제가 멘토가 되고싶어요")
    var levelModeIndex by remember { mutableIntStateOf(0) }
    var timeHour by remember { mutableIntStateOf(6) }
    var timeMinute by remember { mutableIntStateOf(0) }
    var showFindResults by remember { mutableStateOf(false) }

    val cards = listOf(
        MatchingConfigCard("S", "Start Location", "만날 출발 지점을 선택하세요", if (startLocation == null) "현재 위치" else "지도에서 지정됨", Color(0xFFE4E9F2), Color(0xFF111318)),
        MatchingConfigCard("D", "Running Distance", "원하는 러닝 거리를 고르세요", if (distanceKm == 0) "거리 무관" else "$distanceKm km", Color(0xFFFFC80A), Color(0xFF1B1500)),
        MatchingConfigCard("L", "Runner Level Range", levelModes[levelModeIndex], "", Color(0xFF0E8AD9), Color.White),
        MatchingConfigCard("M", "Runner Count", "함께 뛸 인원 수를 정하세요", if (runnerCount == 11) "인원 무관" else "$runnerCount 명", Color(0xFF7D5CFA), Color.White),
        MatchingConfigCard("T", "Time Slot", "원하는 시간대", if (timeHour == -1) "시간 무관" else String.format("%02d:%02d", timeHour, timeMinute), Color(0xFF1F2337), Color.White)
    )

    val runningMatchPosts = remember(distanceKm, runnerCount, levelModeIndex, timeHour, timeMinute, startLocation) {
        listOf(
            RunningMatchPost(
                title = "한강 야간 러닝 ${distanceKm}km",
                summary = "출발 ${if (startLocation == null) "현재 위치 기준" else "지도 지정 위치 기준"} · ${runnerCount}명 모집 중",
                meta = "${String.format("%02d:%02d", timeHour, timeMinute)} 출발 · ${levelModes[levelModeIndex]}",
                accentColor = Color(0xFF1F2337),
                currentMembers = (runnerCount - 1).coerceAtLeast(1),
                maxMembers = runnerCount,
                startHour = timeHour,
                startMinute = timeMinute
            ),
            RunningMatchPost(
                title = "도심 템포런 메이트",
                summary = "중급 러너 환영 · ${distanceKm + 2}km 페이스 러닝",
                meta = "모집 인원 ${runnerCount}명 · 출발 ${String.format("%02d:%02d", timeHour, timeMinute)}",
                accentColor = Color(0xFF0E8AD9),
                currentMembers = runnerCount.coerceAtLeast(2),
                maxMembers = (runnerCount + 2).coerceAtMost(10),
                startHour = timeHour,
                startMinute = timeMinute
            ),
            RunningMatchPost(
                title = "주말 회복 조깅 크루",
                summary = "가볍게 달릴 메이트 모집 게시글",
                meta = "${levelModes[levelModeIndex]} · 함께 ${runnerCount}명",
                accentColor = Color(0xFF7D5CFA),
                currentMembers = (runnerCount / 2).coerceAtLeast(1),
                maxMembers = runnerCount.coerceAtLeast(2),
                startHour = timeHour,
                startMinute = timeMinute
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RunningMapSection(
            modifier = Modifier.matchParentSize(),
            coursePath = coursePath,
            selectedLocation = if (isStartLocationPickerMode) pendingStartLocation else startLocation,
            onMapTap = { tapped ->
                if (isStartLocationPickerMode) {
                    pendingStartLocation = tapped
                }
            }
        )

        Text(
            text = "Runner’s High",
            fontFamily = RacingSansOne,
            fontSize = 30.sp,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 26.dp)
        )

        Surface(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 16.dp)
                .zIndex(20f),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "닫기",
                tint = Color(0xFFE83838),
                modifier = Modifier.padding(10.dp)
            )
        }


        if (showFindResults) {
            Log.d("MatchingSearch", "오버레이로 전달하는 값: distance=$distanceKm, intent=${levelModes[levelModeIndex]}, count=$runnerCount, time=$timeHour:$timeMinute")
            RunningMateResultsOverlay(
                viewModel = viewModel,
                posts = runningMatchPosts,
                onBack = { showFindResults = false },
                onJoinPost = { joinedPost: MatchingRoomDto ->
                    // "07:00" 같은 문자열에서 시간과 분을 분리하는 로직
                    val timeSlot = joinedPost.desiredTimeSlot

                    // 1. null이 아닌 값을 우선적으로 선택 (방 생성 시 쓰인 필드 우선)
                    val rawTime = timeSlot.startTimeAlias // 혹은 startTime
                        ?: timeSlot.startTime
                        ?: "-1:00" // 둘 다 없으면 기본값

                    Log.d("TimeDebug", "참여 시 선택된 원본 시간값: $rawTime")
                    val timeParts = rawTime.split(":")
                    val hour = timeParts?.getOrNull(0)?.toIntOrNull() ?: 0
                    val minute = timeParts?.getOrNull(1)?.toIntOrNull() ?: 0

                    onMatchConfirmed(
                        ActiveRunningMateReservation(
                            roomId = joinedPost.roomId,
                            title = joinedPost.roomName, // title -> roomName
                            participationType = MatchParticipationType.JOINED,
                            // currentMembers -> currentParticipantCount
                            currentMembers = joinedPost.currentParticipantCount,
                            maxMembers = joinedPost.maxParticipants, // maxMembers -> maxParticipants
                            startHour = hour,
                            startMinute = minute
                        )
                    )
                },
                distanceKm = distanceKm,
                matchingIntent = levelModes[levelModeIndex],
                maxParticipants = runnerCount,
                desiredStartTime = String.format("%02d:%02d", timeHour, timeMinute),
                userId = userUuid
            ) //RunningMateResultsOverlay
            return@Box
        }

        if (isStartLocationPickerMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp)
            ) {
                TextField(
                    value = locationSearchQuery,
                    onValueChange = { locationSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("도로명 주소로 검색") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "주소 검색",
                            tint = Color(0xFF1F2337)
                        )
                    }
                )
            }

            Button(
                onClick = {
                    pendingStartLocation?.let {
                        startLocation = it
                        isStartLocationPickerMode = false
                    }
                },
                enabled = pendingStartLocation != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111318))
            ) {
                Text("시작 위치 확정", color = Color.White)
            }

            return@Box
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy((-8).dp)
        ) {
            cards.forEachIndexed { index, card ->
                RunningMateConfigCard(
                    card = card,
                    isSelected = selectedCardIndex == index,
                    onClick = {
                        selectedCardIndex = index
                        when (index) {
                            0 -> {
                                pendingStartLocation = startLocation
                                isStartLocationPickerMode = true
                            }
                            1 -> showDistancePicker = true
                            3 -> showRunnerCountPicker = true
                            4 -> showTimePicker = true
                        }
                    },
                    modifier = if (index == 2) {
                        Modifier.pointerInput(levelModeIndex) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    if (dragAmount > 20) {
                                        levelModeIndex = (levelModeIndex - 1).coerceAtLeast(0)
                                    } else if (dragAmount < -20) {
                                        levelModeIndex = (levelModeIndex + 1).coerceAtMost(levelModes.lastIndex)
                                    }
                                }
                            )
                        }
                    } else Modifier
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (mode == RunningMateEntryMode.CREATE) {
                        // 1. 방 정보 준비
                        val currentUserId = userUuid.trim()
                        if (currentUserId.isBlank()) {
                            Toast.makeText(
                                context,
                                "로그인이 필요합니다. 다시 로그인한 뒤 이용해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Surface
                        }
                        val location = startLocation ?: LatLng(37.5665, 126.978) // 위치 미지정 시 기본값
                        val finalRoomName = if (distanceKm == 0) "프리 런 매칭" else "내가 만든 ${distanceKm}km 러닝 매칭"
                        // 2. ViewModel의 생성 함수 호출
                        viewModel.createMatchingRoom(
                            userUuid = currentUserId,
                            roomName = finalRoomName,
                            location = location,
                            distance = distanceKm,
                            intentIndex = levelModeIndex,
                            maxPeople = runnerCount,
                            hour = timeHour,
                            minute = timeMinute,
                            onSuccess = {response ->
                                // 서버 성공 응답을 받은 후 실행될 콜백
                                onMatchConfirmed(
                                    ActiveRunningMateReservation(
                                        roomId = response.roomId,
                                        title = "내가 만든 ${distanceKm}km 러닝 매칭",
                                        participationType = MatchParticipationType.CREATED,
                                        currentMembers = 1,
                                        maxMembers = runnerCount,
                                        startHour = timeHour,
                                        startMinute = timeMinute
                                    )
                                )
                                // (옵션) 성공 토스트 메시지나 화면 닫기 처리
                                onClose()
                            }
                        )
                    } else {
                        // "찾기" 모드 로직 (이미 구현됨)
                        showFindResults = true
                    }
                },
                color = Color(0xFF111318),
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 10.dp
            ) {
                Text(
                    text = if (mode == RunningMateEntryMode.CREATE) "러닝 매칭 만들기" else "러닝 매칭 찾기",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
                    .clickable { onBack() },
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "이전 화면으로",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (showDistancePicker) {
        KmPickerDialog(
            title = "목표 거리 설정",
            range = 0..42, // 0부터 시작
            initialValue = distanceKm,
            displayMapper = { value -> if (value == 0) "무관" else "${value}km" }, // UI에 표시될 때만 변환
            // 1. 필수 인자 추가
            panelColor = Color(0xFFFFC80A),
            // 2. 필수 인자 추가
            onDismiss = { showDistancePicker = false },
            onConfirm = {
                distanceKm = it // 무관 선택 시 0이 저장됨
                showDistancePicker = false
            }
        )
    }

    if (showRunnerCountPicker) {
        KmPickerDialog(
            title = "러닝 인원 수 설정",
            range = 2..11,
            initialValue = runnerCount,
            displayMapper = { value -> if (value == 11) "무관" else "${value}명" },
            panelColor = Color(0xFF7D5CFA),
            onDismiss = { showRunnerCountPicker = false },
            onConfirm = {
                runnerCount = it
                showRunnerCountPicker = false
            }
        )
    }
    val modeA = SearchMode(search = true)
    if (showTimePicker) {
        TimeWheelPickerDialog(
            panelColor = Color(0xFF1F2337),
            initialHour = timeHour,
            initialMinute = timeMinute,
            isSearch= modeA.search,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                timeHour =hour
                timeMinute = minute
                showTimePicker = false
            }
        )
    }

}
//onJoinPost
@Composable
private fun RunningMateResultsOverlay(
    viewModel: RunningViewModel,
    posts: List<RunningMatchPost>,
    onBack: () -> Unit,
    onJoinPost: (MatchingRoomDto) -> Unit,
    distanceKm: Int,
    matchingIntent: String,
    maxParticipants: Int,
    desiredStartTime: String,
    userId: String
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Log.d("MatchingSearch", "Overlay 화면 진입 - 데이터 요청 시작")
        viewModel.fetchMatchingRooms(distanceKm = distanceKm,
            matchingIntent = matchingIntent,
            maxParticipants = maxParticipants,
            desiredStartTime = desiredStartTime) //
    }
    val rooms by viewModel.matchingRooms.collectAsState()
    val isLoading by viewModel.matchingRoomsLoading.collectAsState()
    val errorMessage by viewModel.matchingRoomsError.collectAsState()
    val activeFilters by viewModel.activeFilters.collectAsState()
    var joiningRoomId by remember { mutableStateOf<String?>(null) }
    val uniqueRooms = remember(rooms) {
        rooms.distinctBy { room ->
            listOf(
                room.hostUserId,
                room.roomName,
                room.distanceKm.toString(),
                room.maxParticipants.toString(),
                room.desiredTimeSlot.startTimeAlias ?: room.desiredTimeSlot.startTime.orEmpty(),
                room.startPoint.name,
                room.startPoint.lat.toString(),
                room.startPoint.lng.toString()
            ).joinToString("|")
        }
    }
    if (isLoading) {
        RunningMateLoadingScreen()
        return
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.mate_asphalt_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.12f))
                .padding(top = 112.dp, start = 16.dp, end = 16.dp, bottom = 20.dp)
        ) {

        Text(
            text = "현재 모집 중인 러닝",
            fontFamily = RacingSansOne,
            fontSize = 28.sp,
            color = mateWarmWhite
        )
        Spacer(modifier = Modifier.height(12.dp))
        // --- 필터 칩 표시 영역 (기존 로직 유지하며 추가) ---
        //val activeFilters by viewModel.activeFilters.collectAsState()

        if (activeFilters.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp) // 줄바꿈 시 간격
            ) {
                activeFilters.forEach { (key, value) ->
                    // 위치 관련 기본 필터는 칩에서 제외
                    if (key != "currentLat" && key != "currentLng" && key != "radiusKm") {
                        FilterChip(
                            selected = true,
                            onClick = { /* 칩 자체 클릭 로직 비움 */ },
                            label = {
                                Text(
                                    text = value,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "제거",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { /*viewModel.removeFilter(key)*/ } // X 누르면 해당 필터만 삭제
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = mateRoomOrange,
                                selectedLabelColor = mateWarmWhite,
                                selectedTrailingIconColor = mateWarmWhite
                            ),
                            border = null
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
       // val rooms: List<MatchingRoomDto> by viewModel.matchingRooms.collectAsState()

        errorMessage?.let { message ->
            Text(
                text = message,
                color = mateWarmWhite,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // rooms(서버데이터)를 가져와서, 기존 이름인 'post'로 사용합니다.
            items(
                items = uniqueRooms,
                key = { post -> post.roomId.ifBlank { post.roomName } }
            ) { post: MatchingRoomDto ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = mateRoomOrange),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Surface(
                            // post.accentColor가 없으므로 기본색 지정, 나머지 구조는 기존과 동일
                            color = Color.Black.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "모집중",
                                color = mateWarmWhite,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // 🔵 여기만 서버 DTO 명칭(roomName)으로 살짝 변경
                        Text(if (post.distanceKm == 0) "프리 런 매칭" else post.roomName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF111318))

                        Spacer(modifier = Modifier.height(6.dp))

                        // 🔵 summary 대신 인원 정보
                        Text(text = "현재 ${if (post.maxParticipants == 11) "인원 무관" else "${post.currentParticipantCount}/${post.maxParticipants}명"}", color = Color(0xFF4F566B), fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(6.dp))

                        // 🔵 meta 대신 서버 시간/장소 조합
                        val timeString = if (post.desiredTimeSlot.startTimeAlias == "-1:00" || post.desiredTimeSlot.startTimeAlias == "무관") {
                            "시간 무관"
                        } else {
                            Log.d("TimeDebug", "startTimeAlias의 실제 값: ${post.desiredTimeSlot.startTimeAlias}")
                            "${post.desiredTimeSlot.startTimeAlias}"//
                        }
                        val distanceString = if (post.distanceKm == 0) {
                            "거리 무관"
                        } else {
                            "${post.distanceKm}km"
                        }
                        val metaInfo = "$timeString · $distanceString · ${post.startPoint.name}"
                        Text(text = metaInfo, color = mateWarmWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        val isJoining = joiningRoomId == post.roomId
                        Button(
                            onClick = {
                                if (isJoining) return@Button
                                if (userId.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "로그인이 필요합니다. 다시 로그인한 뒤 이용해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                joiningRoomId = post.roomId
                                viewModel.joinMatchingRoom(
                                    roomId = post.roomId,
                                    userId = userId,
                                    onSuccess = { joinedRoom ->
                                        joiningRoomId = null
                                        onJoinPost(joinedRoom)
                                    },
                                    onError = { message ->
                                        joiningRoomId = null
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        Log.e("Matching", "방 참여 실패: $message")
                                    }
                                )
                            },
                            enabled = !isJoining,
                            colors = ButtonDefaults.buttonColors(containerColor = mateJoinRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isJoining) "참여 중..." else "이 매칭 참여하기", color = mateWarmWhite)
                        }
                    }
                }
            }
        }
        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .navigationBarsPadding(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111318))
        ) {
            Text("조건 다시 설정", color = Color.White)
        }
        }
    }
}

private val mateWarmWhite = Color(0xFFFFF8E8)
private val mateRoomOrange = Color(0xCCFF8200)
private val mateJoinRed = Color(0xB8D7262D)

@Composable
private fun ActiveReservationCard(
    reservation: ActiveRunningMateReservation,
    viewModel: RunningViewModel, // ViewModel 주입
    currentUserId: String // 현재 접속 중인 유저 ID
) {
    val context = LocalContext.current
    fun requireLoggedIn(): Boolean {
        if (currentUserId.isNotBlank()) return true
        Toast.makeText(
            context,
            "로그인이 필요합니다. 다시 로그인한 뒤 이용해주세요.",
            Toast.LENGTH_SHORT
        ).show()
        return false
    }

    // 기존 로직 유지
    val participationText = if (reservation.participationType == MatchParticipationType.CREATED) {
        "내가 만든 매칭"
    } else {
        "참여 중인 매칭"
    }

    // 드롭다운 상태 관리
    var expanded by remember { mutableStateOf(false) }

    // 서버에서 받은 초기 상태값에 따른 표시 텍스트 설정

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xEEFFFFFF)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // 우측 상단 드롭다운 버튼을 겹치기 위해 Box 사용
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. 기존 정보 유지
                Text(
                    text = participationText,
                    color = Color(0xFF1F2337),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = reservation.title,
                    color = Color(0xFF111318),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${if (reservation.maxMembers == 11) "인원 무관" else "현재 ${reservation.currentMembers}/${reservation.maxMembers}명 모였어요"} ",
                    color = Color(0xFF0E8AD9),
                    fontWeight = FontWeight.SemiBold
                )
                Log.d("TimeDebug", "현재 startHour 값: ${reservation.startHour}")
                val timeDisplay = if (reservation.startHour == -1) {
                    "시간 무관, "
                } else {
                    formatKoreanTime(reservation.startHour, reservation.startMinute)
                }
                Text(
                    text = "$timeDisplay 러닝 메이트 예약 · 30분 전 알림",
                    color = Color(0xFF4F566B),
                    fontSize = 13.sp
                )

                // 2. 우측 하단 방 나가기 버튼 추가
                if (reservation.participationType == MatchParticipationType.JOINED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (!requireLoggedIn()) return@TextButton
                            viewModel.leaveMatchingRoom(reservation.roomId, currentUserId)
                                  },
                        contentPadding = PaddingValues(0.dp) // 버튼 여백 제거로 텍스트 정렬
                    ) {
                        Text(
                            text = "방 나가기",
                            color = Color(0xFFE53935), // 빨간색 계열
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }}

            // 3. 우측 상단 드롭다운 버튼 추가 (내가 만든 매칭일 때만 표시)
            if (reservation.participationType == MatchParticipationType.CREATED) {
                // 현재 상태를 저장할 변수 (초기값은 서버 데이터)
                var currentStatus by remember { mutableStateOf("모집 중") }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                ) {
                    // 이미지 스타일의 텍스트 + 역삼각형 버튼
                    Row(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = currentStatus, // 선택된 항목이 여기에 표시됨
                            color = Color(0xFF0E8AD9), //
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF0E8AD9),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("모집 중", fontSize = 14.sp) },
                            onClick = {
                                if (!requireLoggedIn()) return@DropdownMenuItem
                                currentStatus = "모집 중" // UI 텍스트 변경
                                viewModel.updateRoomStatus(reservation.roomId, currentUserId, "open")
                                expanded = false

                            }
                        )
                        DropdownMenuItem(
                            text = { Text("마감", fontSize = 14.sp) },
                            onClick = {
                                if (!requireLoggedIn()) return@DropdownMenuItem
                                currentStatus = "마감" // UI 텍스트 변경
                                viewModel.updateRoomStatus(reservation.roomId, currentUserId, "closed")
                                expanded = false

                            }
                        )

                        DropdownMenuItem(
                            text = { Text("취소", fontSize = 14.sp) },
                            onClick = {
                                if (!requireLoggedIn()) return@DropdownMenuItem
                                currentStatus = "취소" // UI 텍스트 변경
                                viewModel.updateRoomStatus(reservation.roomId, currentUserId, "cancelled")
                                expanded = false

                            }
                        )
                        DropdownMenuItem(
                            text = { Text("완료", fontSize = 14.sp) },
                            onClick = {
                                if (!requireLoggedIn()) return@DropdownMenuItem
                                currentStatus = "완료" // UI 텍스트 변경
                                viewModel.updateRoomStatus(reservation.roomId, currentUserId, "completed")
                                expanded = false

                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatKoreanTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "오전" else "오후"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$amPm $displayHour:${String.format("%02d", minute)}"
}

private fun styleTimeNumberPicker(numberPicker: NumberPicker) {
    val textColor = android.graphics.Color.WHITE

    for (i in 0 until numberPicker.childCount) {
        val child = numberPicker.getChildAt(i)
        if (child is EditText) {
            child.setTextColor(textColor)
            child.textSize = 24f
        }
    }

    runCatching {
        val selectorWheelPaintField = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
        selectorWheelPaintField.isAccessible = true
        val paint = selectorWheelPaintField.get(numberPicker) as android.graphics.Paint
        paint.color = textColor
        paint.alpha = 255
        paint.textSize = 48f
    }

    runCatching {
        val inputTextField = NumberPicker::class.java.getDeclaredField("mInputText")
        inputTextField.isAccessible = true
        val inputText = inputTextField.get(numberPicker) as? EditText
        inputText?.setTextColor(textColor)
    }

    numberPicker.invalidate()
    numberPicker.requestLayout()
}

@Composable
private fun TimeWheelPickerDialog(
    panelColor: Color,
    initialHour: Int,
    initialMinute: Int,
    isSearch: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var pickedHour by remember { mutableIntStateOf(initialHour) }
    var pickedMinute by remember { mutableIntStateOf((initialMinute / 5) * 5) }

    // 0번째는 "무관", 나머지 1~24번째는 0~23시
    val hourDisplayValues = if (isSearch) {
        arrayOf("무관") + Array(24) { "${it}" }
    } else {
        Array(24) { "${it}" }
    }
    val minuiteDisplayValues = if (isSearch) {
        Array(12) { i -> "${i * 5}" }
    } else {
        Array(60) { "${it}" }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = panelColor,
        title = {
            Text(
                text = "원하는 시간대 설정",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = 24 // 0~24 인덱스
                            displayedValues = hourDisplayValues
                            // -1일 경우 0번째 인덱스(무관)를 선택
                            value = if (pickedHour == -1) 0 else pickedHour + 1
                            setOnValueChangedListener { _, _, newVal ->
                                pickedHour = if (newVal == 0) -1 else newVal - 1
                            }
                        }
                    },
                    update = { picker ->
                        picker.displayedValues = hourDisplayValues
                        val targetValue = if (pickedHour == -1) 0 else pickedHour + 1
                        if (picker.value != targetValue) picker.value = targetValue
                    },
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = 11 // 0~55분까지 12개 항목
                            displayedValues = minuiteDisplayValues
                            wrapSelectorWheel = true
                            setOnValueChangedListener { _, _, newVal ->
                                // 휠이 바뀔 때마다 5분 단위로 값 업데이트
                                pickedMinute = newVal * 5
                            }
                            styleTimeNumberPicker(this)
                        }
                    },
                    update = { picker ->
                        // 더 이상 isSearch 분기 처리 없이 항상 동일한 5분 단위로 유지
                        val minuteIndex = (pickedMinute / 5).coerceIn(0, 11)
                        if (picker.value != minuteIndex) {
                            picker.value = minuteIndex
                        }
                        styleTimeNumberPicker(picker)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickedHour, pickedMinute) }) {
                Text("적용", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.White)
            }
        }
    )
}

@Composable
private fun KmPickerDialog(
    title: String,
    range: IntRange,
    initialValue: Int,
    panelColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,

    displayMapper: (Int) -> String = { it.toString() }
) {
    var selected by remember { mutableIntStateOf(initialValue.coerceIn(range.first, range.last)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = panelColor,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = range.first
                        maxValue = range.last
                        displayedValues = range.map { displayMapper(it) }.toTypedArray()
                        value = selected
                        wrapSelectorWheel = false
                        setOnValueChangedListener { _, _, newVal -> selected = newVal }
                    }
                },
                update = { picker ->
                    picker.minValue = range.first
                    picker.maxValue = range.last
                    picker.displayedValues = range.map { displayMapper(it) }.toTypedArray()
                    if (picker.value != selected) picker.value = selected
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("적용", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Black)
            }
        }
    )
}

@Composable
private fun RunningMateConfigCard(
    card: MatchingConfigCard,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val lifted = isSelected || pressed || focused

    val scale by animateFloatAsState(
        targetValue = if (lifted) 1.07f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "cardScale"
    )
    val offsetY by animateDpAsState(
        targetValue = if (lifted) (-8).dp else 0.dp,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "cardOffsetY"
    )
    val elevation by animateDpAsState(
        targetValue = if (lifted) 12.dp else 6.dp,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "cardElevation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .zIndex(if (lifted) 1f else 0f)
            .offset(y = offsetY)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = card.cardColor,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = card.textColor.copy(alpha = 0.16f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = card.iconLabel,
                        color = card.textColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.title,
                    color = card.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = card.description,
                    color = card.textColor.copy(alpha = 0.82f),
                    fontSize = 13.sp
                )
            }

            Text(
                text = card.value,
                color = card.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MateActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RunnerPresenceBadge(
    modifier: Modifier = Modifier,
    label: String,
    iconTint: Color
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PersonPinCircle,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                color = Color(0xFF293040),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}//val viewModel data class CreateMatchingRoomRequest iewModel.createMatchingRoom 내가 만든 매칭 출발 위치 시작 위치
