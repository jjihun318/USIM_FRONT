package com.pack.info_2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pack.info_2.api.RetrofitClient
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // 레벨 UI 요소
    private lateinit var progressBar: ProgressBar
    private lateinit var expText: TextView

    // 컨디션 UI 요소
    private lateinit var conditionLevel: TextView

    // 미션 UI 요소들 (3개 묶음)
    // 미션 1
    private lateinit var lvTitle1: TextView
    private lateinit var lvDescription1: TextView
    private lateinit var lvExp1: TextView
    private lateinit var lvProgress1: TextView
    private lateinit var missionCategory1: TextView
    private lateinit var lvRate1: ProgressBar
    private lateinit var missionLayout1: View
    private lateinit var icCheckImage1: ImageView

    // 미션 2
    private lateinit var lvTitle2: TextView
    private lateinit var lvDescription2: TextView
    private lateinit var lvExp2: TextView
    private lateinit var lvProgress2: TextView
    private lateinit var missionCategory2: TextView
    private lateinit var lvRate2: ProgressBar
    private lateinit var missionLayout2: View
    private lateinit var icCheckImage2: ImageView

    // 미션 3
    private lateinit var lvTitle3: TextView
    private lateinit var lvDescription3: TextView
    private lateinit var lvExp3: TextView
    private lateinit var lvProgress3: TextView
    private lateinit var missionCategory3: TextView
    private lateinit var lvRate3: ProgressBar
    private lateinit var missionLayout3: View
    private lateinit var icCheckImage3: ImageView

    // 레벨 기본값
    private val EXP_PER_LEVEL = 1000
    private val DEFAULT_EXP = 450
    private val DEFAULT_LEVEL = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        window.statusBarColor = Color.parseColor("#CCFF00")

        // 레벨 UI 초기화
        progressBar = findViewById(R.id.level_progress_bar)
        expText = findViewById(R.id.level_progress)

        // 컨디션 UI 초기화
        conditionLevel = findViewById(R.id.condition_level)
        // 🌟 XML에 설정된 기본값이 그대로 유지됩니다

        // 🌟 레벨 기본값 적용
        progressBar.max = EXP_PER_LEVEL
        progressBar.progress = DEFAULT_EXP
        expText.text = "${DEFAULT_EXP}/${EXP_PER_LEVEL} EXP (Lv. ${DEFAULT_LEVEL})"

        // 미션 UI 초기화
        initMissionViews()

        // 🌟 XML에 설정된 값이 그대로 표시됩니다

        val clickableLayout = findViewById<LinearLayout>(R.id.linear_layout_clickable)
        clickableLayout.setOnClickListener {
            val intent = Intent(this, BadgeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUserId = 123
        fetchUserLevel(currentUserId)
        fetchUserMissions(currentUserId)
        fetchUserCondition(currentUserId)
    }

    private fun initMissionViews() {
        // 미션 1 UI 요소 연결
        missionLayout1 = findViewById(R.id.lv_title_1)
        lvTitle1 = findViewById(R.id.lv_title_1)
        lvDescription1 = findViewById(R.id.lv_description_1)
        lvExp1 = findViewById(R.id.lv_exp_1)
        lvProgress1 = findViewById(R.id.lv_progress_1)
        lvRate1 = findViewById(R.id.lv_rate_1)
        icCheckImage1 = findViewById(R.id.ic_check_image1)

        // 미션 2 UI 요소 연결
        missionLayout2 = findViewById(R.id.lv_title_2)
        lvTitle2 = findViewById(R.id.lv_title_2)
        lvDescription2 = findViewById(R.id.lv_description_2)
        lvExp2 = findViewById(R.id.lv_exp_2)
        lvProgress2 = findViewById(R.id.lv_progress_2)
        lvRate2 = findViewById(R.id.lv_rate_2)
        icCheckImage2 = findViewById(R.id.ic_check_image2)

        // 미션 3 UI 요소 연결
        missionLayout3 = findViewById(R.id.lv_title_3)
        lvTitle3 = findViewById(R.id.lv_title_3)
        lvDescription3 = findViewById(R.id.lv_description_3)
        lvExp3 = findViewById(R.id.lv_exp_3)
        lvProgress3 = findViewById(R.id.lv_progress_3)
        lvRate3 = findViewById(R.id.lv_rate_3)
        icCheckImage3 = findViewById(R.id.ic_check_image3)

        // 🌟 미션 레이아웃 visibility는 XML에서 설정한 대로 유지
        // (XML에서 visible로 설정했다면 그대로 보임)
    }

    private fun fetchUserLevel(userId: Int) {
        launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.userService.getUserLevel(userId)

                if (response.isSuccessful) {
                    val userLevel = response.body()
                    Log.d("API_CALL", "유저 레벨 데이터 성공: $userLevel")

                    withContext(Dispatchers.Main) {
                        updateLevelUI(userLevel)
                    }
                } else {
                    Log.e("API_CALL", "레벨 API 호출 실패: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_CALL", "레벨 네트워크 오류 발생", e)
            }
        }
    }

    // 미션 목록 조회
    private fun fetchUserMissions(userId: Int) {
        launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.userService.getUserMissions(userId)

                if (response.isSuccessful) {
                    val missions = response.body()
                    Log.d("API_CALL", "미션 데이터 성공: $missions")

                    // API 호출 성공 시에만 UI 업데이트
                    withContext(Dispatchers.Main) {
                        updateMissionUI(missions)
                    }
                } else {
                    // API 호출 실패 시: XML 값 유지
                    Log.e("API_CALL", "미션 API 호출 실패: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                // 네트워크 오류 시: XML 값 유지
                Log.e("API_CALL", "미션 네트워크 오류 발생", e)
            }
        }
    }

    // 컨디션 조회
    private fun fetchUserCondition(userId: Int) {
        launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.userService.getUserCondition(userId)

                if (response.isSuccessful) {
                    val condition = response.body()
                    Log.d("API_CALL", "컨디션 데이터 성공: $condition")

                    withContext(Dispatchers.Main) {
                        updateConditionUI(condition)
                    }
                } else {
                    // API 호출 실패 시: XML 값 유지
                    Log.e("API_CALL", "컨디션 API 호출 실패: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                // 네트워크 오류 시: XML 값 유지
                Log.e("API_CALL", "컨디션 네트워크 오류 발생", e)
            }
        }
    }

    private fun updateLevelUI(userLevel: UserLevel?) {
        if (userLevel == null) return

        val currentLevelExp = userLevel.experiencePoints % EXP_PER_LEVEL

        progressBar.progress = currentLevelExp
        expText.text = "${currentLevelExp}/${EXP_PER_LEVEL} EXP (Lv. ${userLevel.level})"
    }

    // 미션 UI 업데이트 (API 성공 시에만 호출됨)
    private fun updateMissionUI(missions: List<Mission>?) {
        if (missions == null || missions.isEmpty()) {
            Log.w("MISSION_UI", "미션 데이터가 없습니다.")
            return
        }

        // 첫 번째 미션
        if (missions.size > 0) {
            val mission1 = missions[0]
            lvTitle1.text = mission1.missionName
            lvDescription1.text = mission1.missionDescription
            lvProgress1.text = mission1.progressStatus
            missionCategory1.text = mission1.missionCategory
            lvRate1.max = 100
            lvRate1.progress = mission1.gaugeRatio
            missionLayout1.visibility = View.VISIBLE

            // 미션 완료 체크
            if (mission1.gaugeRatio >= 100) {
                lvRate1.visibility = View.GONE
                icCheckImage1.visibility = View.VISIBLE
                lvExp1.text = "COMPLETE!"
            } else {
                lvRate1.visibility = View.VISIBLE
                icCheckImage1.visibility = View.GONE
                lvExp1.text = "+${mission1.expPoints} EXP"
            }
        }

        // 두 번째 미션
        if (missions.size > 1) {
            val mission2 = missions[1]
            lvTitle2.text = mission2.missionName
            lvDescription2.text = mission2.missionDescription
            lvProgress2.text = mission2.progressStatus
            missionCategory2.text = mission2.missionCategory
            lvRate2.max = 100
            lvRate2.progress = mission2.gaugeRatio
            missionLayout2.visibility = View.VISIBLE

            // 미션 완료 체크
            if (mission2.gaugeRatio >= 100) {
                lvRate2.visibility = View.GONE
                icCheckImage2.visibility = View.VISIBLE
                lvExp2.text = "COMPLETE!"
            } else {
                lvRate2.visibility = View.VISIBLE
                icCheckImage2.visibility = View.GONE
                lvExp2.text = "+${mission2.expPoints} EXP"
            }
        }

        // 세 번째 미션
        if (missions.size > 2) {
            val mission3 = missions[2]
            lvTitle3.text = mission3.missionName
            lvDescription3.text = mission3.missionDescription
            lvProgress3.text = mission3.progressStatus
            missionCategory3.text = mission3.missionCategory
            lvRate3.max = 100
            lvRate3.progress = mission3.gaugeRatio
            missionLayout3.visibility = View.VISIBLE

            // 미션 완료 체크
            if (mission3.gaugeRatio >= 100) {
                lvRate3.visibility = View.GONE
                icCheckImage3.visibility = View.VISIBLE
                lvExp3.text = "COMPLETE!"
            } else {
                lvRate3.visibility = View.VISIBLE
                icCheckImage3.visibility = View.GONE
                lvExp3.text = "+${mission3.expPoints} EXP"
            }
        }
    }

    // 컨디션 UI 업데이트
    private fun updateConditionUI(condition: UserCondition?) {
        if (condition == null) {
            Log.w("CONDITION_UI", "컨디션 데이터가 없습니다.")
            return
        }

        conditionLevel.text = condition.conditionLevel
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}