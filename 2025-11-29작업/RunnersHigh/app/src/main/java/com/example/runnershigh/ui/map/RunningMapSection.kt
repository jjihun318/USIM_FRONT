package com.example.runnershigh.ui.map

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons

// FusedLocationSource에서 내부적으로 쓰는 requestCode,
// 실제로 onRequestPermissionsResult를 안 써도 상관없지만 상징적으로 하나 둠
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

/**
 * 러닝 메인 화면의 네이버 지도:
 * - 진입 시 위치 권한을 요청
 * - 권한 허용 시 FusedLocationSource + LocationTrackingMode.Follow 세팅
 * - 처음에는 lastLocation으로 카메라를 한 번 이동
 * - 왼쪽 아래 조준선 버튼을 누르면 네이버 지도 기본 기능대로 내 위치를 따라감
 */
@Composable
fun RunningMapSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as Activity

    val mapView = rememberMapViewWithLifecycle()
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationSource = remember {
        FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
    }

    var isMapInitialized by remember { mutableStateOf(false) }

    // 🔹 런타임 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* 결과는 아래에서 다시 checkSelfPermission 으로 확인 */ }

    // 🔹 처음 진입할 때 권한이 없다면 팝업 요청
    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { view ->
            if (!isMapInitialized) {
                view.getMapAsync(object : OnMapReadyCallback {
                    override fun onMapReady(naverMap: NaverMap) {
                        isMapInitialized = true

                        // ▫ 지도 기본 UI
                        naverMap.uiSettings.isZoomControlEnabled = true
                        naverMap.uiSettings.isLocationButtonEnabled = true

                        // 🔹 네이버 지도에 LocationSource + TrackingMode 연결
                        naverMap.locationSource = locationSource
                        naverMap.locationTrackingMode = LocationTrackingMode.Follow

                        val hasFineLocation = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasFineLocation) {
                            // 권한 OK → 마지막 위치 기준으로 카메라 한 번 이동 + 마커
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        val currentLatLng =
                                            LatLng(location.latitude, location.longitude)

                                        val cameraUpdate =
                                            CameraUpdate.scrollTo(currentLatLng)
                                        naverMap.moveCamera(cameraUpdate)

                                        Marker().apply {
                                            position = currentLatLng
                                            icon = MarkerIcons.GREEN
                                            captionText = "현재 위치"
                                            map = naverMap
                                        }
                                    } else {
                                        moveToDefaultLocation(naverMap)
                                    }
                                }
                                .addOnFailureListener {
                                    moveToDefaultLocation(naverMap)
                                }
                        } else {
                            moveToDefaultLocation(naverMap)
                        }
                    }
                })
            }
        }
    )
}

/**
 * 위치 실패 시 사용하는 기본 카메라 위치 (서울 시청)
 */
private fun moveToDefaultLocation(naverMap: NaverMap) {
    val seoulCityHall = LatLng(37.5665, 126.9780)
    val cameraUpdate = CameraUpdate.scrollTo(seoulCityHall)
    naverMap.moveCamera(cameraUpdate)
}
