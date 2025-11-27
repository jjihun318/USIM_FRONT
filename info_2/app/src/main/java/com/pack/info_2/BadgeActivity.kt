package com.pack.info_2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pack.info_2.api.RetrofitClient
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class BadgeActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // 배지 리스트 UI 요소들 (badge_1 ~ badge_5)
    private lateinit var badgeTitle1: TextView
    private lateinit var badgeDescription1: TextView
    private lateinit var badgeDetail1: TextView
    private lateinit var badgeProgressBar1: ProgressBar
    private lateinit var badgeProgress1: TextView
    private lateinit var badgeLayout1: View

    private lateinit var badgeTitle2: TextView
    private lateinit var badgeDescription2: TextView
    private lateinit var badgeDetail2: TextView
    private lateinit var badgeProgressBar2: ProgressBar
    private lateinit var badgeProgress2: TextView
    private lateinit var badgeLayout2: View

    private lateinit var badgeTitle3: TextView
    private lateinit var badgeDescription3: TextView
    private lateinit var badgeDetail3: TextView
    private lateinit var badgeProgressBar3: ProgressBar
    private lateinit var badgeProgress3: TextView
    private lateinit var badgeLayout3: View

    private lateinit var badgeTitle4: TextView
    private lateinit var badgeDescription4: TextView
    private lateinit var badgeDetail4: TextView
    private lateinit var badgeProgressBar4: ProgressBar
    private lateinit var badgeProgress4: TextView
    private lateinit var badgeLayout4: View

    private lateinit var badgeTitle5: TextView
    private lateinit var badgeDescription5: TextView
    private lateinit var badgeDetail5: TextView
    private lateinit var badgeProgressBar5: ProgressBar
    private lateinit var badgeProgress5: TextView
    private lateinit var badgeLayout5: View

    // 획득한 배지 UI 요소들 (badge_6 ~ badge_8)
    private lateinit var badgeTitle6: TextView
    private lateinit var badgeDescription6: TextView
    private lateinit var badgeDate6: TextView
    private lateinit var badgeLayout6: View

    private lateinit var badgeTitle7: TextView
    private lateinit var badgeDescription7: TextView
    private lateinit var badgeDate7: TextView
    private lateinit var badgeLayout7: View

    private lateinit var badgeTitle8: TextView
    private lateinit var badgeDescription8: TextView
    private lateinit var badgeDate8: TextView
    private lateinit var badgeLayout8: View

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

        // UI 초기화
        initBadgeViews()
        initAcquiredBadgeViews()

        val currentUserId = 123   // ✅ 실제 userId로 변경 필요

        // ✅ API 호출 (성공 시에만 XML 값을 덮어씁니다)
        fetchAllBadges()
        fetchAcquiredBadges(currentUserId)
    }

    private fun initBadgeViews() {
        // 배지 1
        badgeLayout1 = findViewById(R.id.badge_1)
        badgeTitle1 = findViewById(R.id.badge_title_1)
        badgeDescription1 = findViewById(R.id.badge_description_1)
        badgeDetail1 = findViewById(R.id.badge_detail_1)
        badgeProgressBar1 = findViewById(R.id.badge_progress_bar_1)
        badgeProgress1 = findViewById(R.id.badge_progress_1)

        // 배지 2
        badgeLayout2 = findViewById(R.id.badge_2)
        badgeTitle2 = findViewById(R.id.badge_title_2)
        badgeDescription2 = findViewById(R.id.badge_description_2)
        badgeDetail2 = findViewById(R.id.badge_detail_2)
        badgeProgressBar2 = findViewById(R.id.badge_progress_bar_2)
        badgeProgress2 = findViewById(R.id.badge_progress_2)

        // 배지 3
        badgeLayout3 = findViewById(R.id.badge_3)
        badgeTitle3 = findViewById(R.id.badge_title_3)
        badgeDescription3 = findViewById(R.id.badge_description_3)
        badgeDetail3 = findViewById(R.id.badge_detail_3)
        badgeProgressBar3 = findViewById(R.id.badge_progress_bar_3)
        badgeProgress3 = findViewById(R.id.badge_progress_3)

        // 배지 4
        badgeLayout4 = findViewById(R.id.badge_4)
        badgeTitle4 = findViewById(R.id.badge_title_4)
        badgeDescription4 = findViewById(R.id.badge_description_4)
        badgeDetail4 = findViewById(R.id.badge_detail_4)
        badgeProgressBar4 = findViewById(R.id.badge_progress_bar_4)
        badgeProgress4 = findViewById(R.id.badge_progress_4)

        // 배지 5
        badgeLayout5 = findViewById(R.id.badge_5)
        badgeTitle5 = findViewById(R.id.badge_title_5)
        badgeDescription5 = findViewById(R.id.badge_description_5)
        badgeDetail5 = findViewById(R.id.badge_detail_5)
        badgeProgressBar5 = findViewById(R.id.badge_progress_bar_5)
        badgeProgress5 = findViewById(R.id.badge_progress_5)
    }

    private fun initAcquiredBadgeViews() {
        // 배지 6
        badgeLayout6 = findViewById(R.id.badge_6)
        badgeTitle6 = findViewById(R.id.badge_title_6)
        badgeDescription6 = findViewById(R.id.badge_description_6)
        badgeDate6 = findViewById(R.id.badge_date_6)

        // 배지 7
        badgeLayout7 = findViewById(R.id.badge_7)
        badgeTitle7 = findViewById(R.id.badge_title_7)
        badgeDescription7 = findViewById(R.id.badge_description_7)
        badgeDate7 = findViewById(R.id.badge_date_7)

        // 배지 8
        badgeLayout8 = findViewById(R.id.badge_8)
        badgeTitle8 = findViewById(R.id.badge_title_8)
        badgeDescription8 = findViewById(R.id.badge_description_8)
        badgeDate8 = findViewById(R.id.badge_date_8)
    }

    // ✅ 수정: 전체 배지 목록 조회 (badge_1 ~ badge_5용)
    private fun fetchAllBadges() {
        launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.userService.getAllBadges()

                if (response.isSuccessful) {
                    val badges = response.body()
                    Log.d("API_CALL", "배지 데이터 성공: $badges")

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

    // ✅ 수정: 획득한 배지 조회 (badge_6 ~ badge_8용)
    private fun fetchAcquiredBadges(userId: Int) {
        launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.userService.getAcquiredBadges(userId)

                if (response.isSuccessful) {
                    val acquiredBadges = response.body()
                    Log.d("API_CALL", "획득 배지 데이터 성공: $acquiredBadges")

                    withContext(Dispatchers.Main) {
                        updateAcquiredBadgeUI(acquiredBadges)
                    }
                } else {
                    Log.e("API_CALL", "획득 배지 API 호출 실패: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_CALL", "획득 배지 네트워크 오류 발생", e)
            }
        }
    }

    // 배지 리스트 UI 업데이트 (badge_1 ~ badge_5)
    private fun updateBadgeUI(badges: List<Badge>?) {
        if (badges == null || badges.isEmpty()) {
            Log.w("BADGE_UI", "배지 데이터가 없습니다.")
            return
        }

        if (badges.size > 0) {
            val badge1 = badges[0]
            badgeTitle1.text = badge1.missionName
            badgeDescription1.text = badge1.missionDescription
            badgeDetail1.text = badge1.missionDetail
            badgeProgress1.text = badge1.progressStatus
            badgeProgressBar1.max = 100
            badgeProgressBar1.progress = badge1.gaugeRatio
            badgeLayout1.visibility = View.VISIBLE
        }

        if (badges.size > 1) {
            val badge2 = badges[1]
            badgeTitle2.text = badge2.missionName
            badgeDescription2.text = badge2.missionDescription
            badgeDetail2.text = badge2.missionDetail
            badgeProgress2.text = badge2.progressStatus
            badgeProgressBar2.max = 100
            badgeProgressBar2.progress = badge2.gaugeRatio
            badgeLayout2.visibility = View.VISIBLE
        }

        if (badges.size > 2) {
            val badge3 = badges[2]
            badgeTitle3.text = badge3.missionName
            badgeDescription3.text = badge3.missionDescription
            badgeDetail3.text = badge3.missionDetail
            badgeProgress3.text = badge3.progressStatus
            badgeProgressBar3.max = 100
            badgeProgressBar3.progress = badge3.gaugeRatio
            badgeLayout3.visibility = View.VISIBLE
        }

        if (badges.size > 3) {
            val badge4 = badges[3]
            badgeTitle4.text = badge4.missionName
            badgeDescription4.text = badge4.missionDescription
            badgeDetail4.text = badge4.missionDetail
            badgeProgress4.text = badge4.progressStatus
            badgeProgressBar4.max = 100
            badgeProgressBar4.progress = badge4.gaugeRatio
            badgeLayout4.visibility = View.VISIBLE
        }

        if (badges.size > 4) {
            val badge5 = badges[4]
            badgeTitle5.text = badge5.missionName
            badgeDescription5.text = badge5.missionDescription
            badgeDetail5.text = badge5.missionDetail
            badgeProgress5.text = badge5.progressStatus
            badgeProgressBar5.max = 100
            badgeProgressBar5.progress = badge5.gaugeRatio
            badgeLayout5.visibility = View.VISIBLE
        }
    }

    // 획득한 배지 UI 업데이트 (badge_6 ~ badge_8)
    private fun updateAcquiredBadgeUI(acquiredBadges: List<AcquiredBadge>?) {
        if (acquiredBadges == null || acquiredBadges.isEmpty()) {
            Log.w("ACQUIRED_BADGE_UI", "획득한 배지 데이터가 없습니다.")
            return
        }

        if (acquiredBadges.size > 0) {
            val badge6 = acquiredBadges[0]
            badgeTitle6.text = badge6.missionName
            badgeDescription6.text = badge6.missionDescription
            badgeDate6.text = badge6.acquiredDate
            badgeLayout6.visibility = View.VISIBLE
        }

        if (acquiredBadges.size > 1) {
            val badge7 = acquiredBadges[1]
            badgeTitle7.text = badge7.missionName
            badgeDescription7.text = badge7.missionDescription
            badgeDate7.text = badge7.acquiredDate
            badgeLayout7.visibility = View.VISIBLE
        }

        if (acquiredBadges.size > 2) {
            val badge8 = acquiredBadges[2]
            badgeTitle8.text = badge8.missionName
            badgeDescription8.text = badge8.missionDescription
            badgeDate8.text = badge8.acquiredDate
            badgeLayout8.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUserId = 123

        // ✅ 화면 재진입 시 최신 배지 정보 갱신
        fetchAllBadges()
        fetchAcquiredBadges(currentUserId)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}