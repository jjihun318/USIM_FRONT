package com.pack.myapplication

import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.overlay.Marker
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.view.View
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.pack.myapplication.databinding.ActivityMainBinding
import com.naver.maps.map.util.FusedLocationSource
import android.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.location.LocationListener
import android.location.LocationManager
import com.naver.maps.map.overlay.PolylineOverlay
// import com.pack.myapplication.api.RetrofitClient // ❌ Direction5 API 관련 import 제거
// import kotlinx.coroutines.launch // ❌ Coroutine 관련 import 제거 (API 호출 제거로 불필요)

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    /*private lateinit var binding: ActivityMainBinding*/
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    //!private lateinit var locationSource: FusedLocationSource
    private lateinit var locationManager: LocationManager
    private var currentMarker: com.naver.maps.map.overlay.Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 지도 준비 상태 플래그 추가
    private var isMapReady = false

    // 경로 저장을 위한 리스트와 Polyline 변수
    private val pathPoints = mutableListOf<LatLng>()
    private var polyline: PolylineOverlay? = null



    private var totalDistance = 0.0  //  Double 타입으로 변경: 총 이동 거리 (미터)
    private var isTracking = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    // locationListener를 클래스 멤버로 완성하여 스코프 문제 해결
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {
            if (!isTracking) return // 추적이 중지된 상태면 업데이트 무시

            val latitude = location.latitude
            val longitude = location.longitude
            val currentLatLng = LatLng(latitude, longitude)

            // Polyline 업데이트를 위해 새로운 위치를 추가하기 전에 거리 계산을 수행
            if (pathPoints.size >= 1) {
                // 직전 위치를 가져와서 새로운 위치까지의 거리를 계산합니다.
                val previousPoint = pathPoints.last()
                calculateDistance(previousPoint, currentLatLng) // ⭐ GPS 기반 거리 계산 함수 호출
            }

            // 이동 경로 기록
            pathPoints.add(currentLatLng)

            // Polyline 업데이트 (경로 점이 2개 이상일 때)
            if (pathPoints.size >= 2) {
                polyline?.coords = pathPoints // Polyline 업데이트
            }

            // 지도 중심을 새 위치로 업데이트
            val cameraUpdate = CameraUpdate.scrollTo(currentLatLng)
            naverMap.moveCamera(cameraUpdate)

            // 마커 업데이트 (기존 마커 제거 후 새 마커 추가)
            currentMarker?.map = null
            currentMarker = com.naver.maps.map.overlay.Marker().apply {
                position = currentLatLng
                map = naverMap
                captionText = "현재 위치"
                icon = MarkerIcons.GREEN // 추적 중 마커 색상 변경
            }
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    // --- Distance Calculation ---

    /**
     * ⭐ GPS 기반 거리 계산 함수로 변경 (Direction5 API 호출 제거)
     * NaverMap SDK의 `LatLng.distance(to: LatLng)`를 사용하여 두 지점 간의 거리를 미터(m) 단위로 계산합니다.
     */
    private fun calculateDistance(from: LatLng, to: LatLng) {
        // NaverMap SDK에서 제공하는 두 좌표 간의 거리 계산 함수 사용 (단위: 미터)
        val distanceMeters = from.distanceTo(to)

        // 총 거리에 누적
        totalDistance += distanceMeters

        // UI 업데이트
        updateDistanceUI(totalDistance)
    }

    private fun updateDistanceUI(distance: Double) { //  Double 타입으로 변경
        runOnUiThread {
            val distanceKm = distance / 1000.0 // 미터를 킬로미터로 변환
            val distanceText = String.format("%.2f km", distanceKm)

            findViewById<TextView>(R.id.tvDistance)?.let { textView ->
                textView.text = distanceText //실시간 업데이트 담당
            } ?: run {
                Log.e("DistanceUI", "tvDistance TextView를 찾을 수 없습니다!")
            }
        }
    }

    // --- Activity Lifecycle ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        window.statusBarColor = Color.parseColor("#CCFF00")

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // 버튼 클릭 리스너
        findViewById<FloatingActionButton>(R.id.fabPause).setOnClickListener {
            // 일시정지 로직
            Toast.makeText(this, "일시정지는 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }

        findViewById<FloatingActionButton>(R.id.fabStop).setOnClickListener {
            stopTracking()
        }

        findViewById<FloatingActionButton>(R.id.fabStart).setOnClickListener {
            if (!isMapReady) {
                Toast.makeText(this, "지도를 초기화하는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            showCountdownDialog()

            // UI 변경은 바로 적용
            findViewById<FloatingActionButton>(R.id.fabStart).visibility = View.INVISIBLE
            findViewById<FloatingActionButton>(R.id.fabStop).visibility = View.VISIBLE
            findViewById<FloatingActionButton>(R.id.fabPause).visibility = View.VISIBLE
        }
    }

    // --- Tracking Control Functions ---

    private fun prepareAndStartTracking() {
        // 이전 Polyline이 남아있다면 제거 (안전장치)
        polyline?.map = null
        polyline = null
        pathPoints.clear()

        // 거리 측정 초기화
        totalDistance = 0.0 //  Double 타입으로 초기화
        isTracking = true

        polyline = PolylineOverlay().apply {
            color = Color.GREEN
            width = 20
            this.map = naverMap // naverMap 접근 시점 안전 확보
        }

        startLocationUpdates()
        updateDistanceUI(0.0) //  Double 타입으로 초기 UI 업데이트
    }

    private fun stopTracking() {
        // 정지 로직
        findViewById<FloatingActionButton>(R.id.fabStart).visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.fabStop).visibility = View.INVISIBLE
        findViewById<FloatingActionButton>(R.id.fabPause).visibility = View.INVISIBLE
        isTracking = false

        // 1. 위치 추적 중지
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: Exception) {
            Log.e("StopError", "위치 업데이트 중지 중 오류: ${e.message}")
        }
        // 핵심 수정: 거리 UI를 0.00 km로 업데이트
        updateDistanceUI(0.0)
        // Polyline 제거
        polyline?.map = null
        polyline = null

        // 경로 데이터 초기화
        pathPoints.clear()

        // 현재 마커 제거 및 위치 재설정
        currentMarker?.map = null
        currentMarker = null
        getCurrentLocation()//현재 위치로 시점 이동

        Toast.makeText(this, "경로 추적을 중지합니다.", Toast.LENGTH_SHORT).show()
    }

    // --- Map and Location Setup ---

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        isMapReady = true // 지도 준비 완료 플래그 설정

        // 지도 UI 설정
        naverMap.uiSettings.isZoomControlEnabled = true
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 권한 확인 및 현재 위치 가져오기
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // 카메라 위치 설정 (서울 시청 기준)
        setDefaultLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // 지도 중심을 현재 위치로 설정
                val cameraUpdate = CameraUpdate.scrollTo(currentLatLng)
                naverMap.moveCamera(cameraUpdate)

                currentMarker?.map = null // 기존 마커 제거
                currentMarker = com.naver.maps.map.overlay.Marker().apply {
                    position = currentLatLng
                    map = naverMap
                    icon = MarkerIcons.BLUE
                    captionText = "현재 위치"
                }
            } else {
                setDefaultLocation()
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "위치 권한이 없어 추적을 시작할 수 없습니다.", Toast.LENGTH_LONG).show()
            return
        }

        // 위치 업데이트 설정 (GPS_PROVIDER 사용)
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,  // 1초마다
            0f,    // 0m 이상 이동 시
            locationListener // 클래스 멤버 locationListener 사용
        )
        Toast.makeText(this, "경로 추적을 시작합니다.", Toast.LENGTH_SHORT).show()
    }

    private fun setDefaultLocation() {
        val seoulCity = LatLng(37.5665, 126.9780)
        val cameraUpdate = CameraUpdate.scrollTo(seoulCity)
        naverMap.moveCamera(cameraUpdate)
    }

    // --- Countdown Dialog ---

    private fun showCountdownDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.countdown, null)
        val countdownText = dialogView.findViewById<TextView>(R.id.countdown_text)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        var count = 3
        val timer = Handler(Looper.getMainLooper())

        val countdownRunnable = object : Runnable {
            override fun run() {
                if (count > 0) {
                    countdownText.text = count.toString()
                    count--
                    timer.postDelayed(this, 1000)
                } else {
                    countdownText.text = "GO!"
                    timer.postDelayed({
                        dialog.dismiss()
                        onCountdownFinished() // 카운트다운 완료 후 추적 시작
                    }, 500)
                }
            }
        }
        timer.post(countdownRunnable)
    }

    private fun onCountdownFinished() {
        // 카운트다운이 끝났을 때 실행되는 로직.
        prepareAndStartTracking() // 여기서 실제 추적을 시작합니다.
    }


    // --- System Overrides ---

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                setDefaultLocation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationManager.removeUpdates(locationListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}