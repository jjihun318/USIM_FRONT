package com.pack.info_2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pack.info_2.api.RetrofitClient // API 클라이언트 경로는 그대로 가정
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext




class BadgeActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // 🌟 삭제된 변수: badgeTitle1 ~ badgeLayout5 (수동 UI 요소들은 이제 필요 없음)

    // RecyclerView 및 어댑터 (잠금 배지용) 🌟 추가
    private lateinit var lockedBadgesRecyclerView: RecyclerView
    private lateinit var lockedBadgeAdapter: LockedBadgeAdapter

    // RecyclerView 및 어댑터 (획득 배지용)
    private lateinit var acquiredBadgesRecyclerView: RecyclerView
    private lateinit var acquiredBadgeAdapter: AcquiredBadgeAdapter

    // 획득한 배지 개수 표시
    private lateinit var badgeCountText: TextView

    // 획득한 배지가 없을 때 표시할 뷰
    private lateinit var noBadgeLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        setContentView(R.layout.activity_badge)
        supportActionBar?.hide()

        window.statusBarColor = Color.parseColor("#CCFF00")

        // 뒤로가기 버튼 로직
        val backButton = findViewById<ImageView>(R.id.iv_back)
        backButton.setOnClickListener {
            finish()
        }

        // 🌟 수정: initBadgeViews() 제거 또는 내용 비움
        initRecyclerView()

        val currentUserId = 123

        // API 호출
        fetchAllBadges()
        fetchAcquiredBadges(currentUserId)
    }

    // 🌟 삭제: initBadgeViews() 함수 (더 이상 수동 연결할 요소가 없으므로)

    private fun initRecyclerView() {
        // 획득 배지 RecyclerView 초기화
        acquiredBadgesRecyclerView = findViewById(R.id.acquired_badges_recycler_view)
        acquiredBadgesRecyclerView.layoutManager = LinearLayoutManager(this)
        acquiredBadgeAdapter = AcquiredBadgeAdapter(emptyList()) // AcquiredBadgeAdapter는 기존과 동일하다고 가정
        acquiredBadgesRecyclerView.adapter = acquiredBadgeAdapter

        // 🌟 잠금 배지 RecyclerView 초기화 (새로 추가)
        lockedBadgesRecyclerView = findViewById(R.id.locked_badges_recycler_view)
        lockedBadgesRecyclerView.layoutManager = LinearLayoutManager(this)
        lockedBadgeAdapter = LockedBadgeAdapter(emptyList()) // LockedBadgeAdapter 사용
        lockedBadgesRecyclerView.adapter = lockedBadgeAdapter

        // 스크롤뷰 내에서 RecyclerView가 동작하므로 성능을 위해 nestedScrollingEnabled를 false로 설정합니다.
        // XML에서 설정하지 않았다면 코드로 설정합니다.
        lockedBadgesRecyclerView.isNestedScrollingEnabled = false


        // 배지 개수 텍스트
        badgeCountText = findViewById(R.id.badge_count_text)

        // 배지 없을 때 표시할 레이아웃
        noBadgeLayout = findViewById(R.id.badge_complete_4)
    }

    // 전체 배지 목록 조회 (잠금 배지 목록으로 사용)
    private fun fetchAllBadges() {
        launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.userService.getAllBadges()

                if (response.isSuccessful) {
                    val badges = response.body()
                    Log.d("API_CALL", "전체 배지 데이터 성공: $badges")

                    withContext(Dispatchers.Main) {
                        updateBadgeUI(badges)
                    }
                } else {
                    Log.e("API_CALL", "배지 API 호출 실패: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_CALL", "배지 네트워크 오류 발생", e)
            }
        }
    }

    // 획득한 배지 조회 (RecyclerView용)
    private fun fetchAcquiredBadges(userId: Int) {
        launch(Dispatchers.IO) {
            try {
                // ... (기존 로직 유지) ...
            } catch (e: Exception) {
                // ... (기존 로직 유지) ...
            }
        }
    }

    // 🌟 수정: 배지 리스트 UI 업데이트 (lockedBadgesRecyclerView 사용)
    private fun updateBadgeUI(badges: List<Badge>?) {
        if (badges == null || badges.isEmpty()) {
            Log.w("BADGE_UI", "잠금 배지 데이터가 없습니다.")
            lockedBadgesRecyclerView.visibility = View.GONE
            return
        }

        lockedBadgesRecyclerView.visibility = View.VISIBLE
        // 전체 배지 목록을 어댑터에 넘겨 RecyclerView가 반복 출력하도록 합니다.
        lockedBadgeAdapter.updateBadges(badges)

        // 🌟 기존의 badge_1 ~ badge_5에 데이터를 할당하던 모든 로직은 삭제되었습니다.
    }

    // 획득한 배지 UI 업데이트 (RecyclerView)
    private fun updateAcquiredBadgeUI(acquiredBadges: List<AcquiredBadge>?) {
        // ... (기존 로직 유지: acquiredBadges.size를 badgeCountText에 표시하는 로직 포함) ...
        if (acquiredBadges == null || acquiredBadges.isEmpty()) {
            noBadgeLayout.visibility = View.VISIBLE
            acquiredBadgesRecyclerView.visibility = View.GONE
            badgeCountText.text = "0"
        } else {
            noBadgeLayout.visibility = View.GONE
            acquiredBadgesRecyclerView.visibility = View.VISIBLE
            badgeCountText.text = acquiredBadges.size.toString()
            acquiredBadgeAdapter.updateBadges(acquiredBadges)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUserId = 123
        fetchAllBadges()
        fetchAcquiredBadges(currentUserId)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}