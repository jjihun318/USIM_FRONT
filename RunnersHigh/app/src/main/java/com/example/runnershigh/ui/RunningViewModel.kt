package com.example.runnershigh.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnershigh.data.remote.ApiClient
import com.example.runnershigh.data.repository.RunningRepository
import com.example.runnershigh.domain.model.RunningStats
import kotlinx.coroutines.launch

/**
 * 러닝 세션의 시작/종료 + 마지막 결과를 관리하는 ViewModel
 */
class RunningViewModel(
    // 아직 Hilt 같은 DI 안 쓰니까 여기서 직접 생성
    private val repository: RunningRepository = RunningRepository(ApiClient.runningApi)
) : ViewModel() {

    // 서버에서 받은 세션 ID
    var sessionId by mutableStateOf<String?>(null)
        private set

    // 서버 요청 중 로딩 상태
    var isLoading by mutableStateOf(false)
        private set

    // 에러 메시지
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 마지막 러닝 결과 (결과 화면에서 사용)
    var lastStats: RunningStats? by mutableStateOf(null)
        private set

    /**
     * 러닝 세션 시작
     * - POST /sessions/start
     */
    fun startSession() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val id = repository.startSession()
                sessionId = id
            } catch (e: Exception) {
                errorMessage = e.message ?: "세션 시작에 실패했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 러닝 세션 종료
     * - PATCH /api/sessions/{sessionId}
     *
     * 서버 저장에 실패해도 lastStats 는 항상 채워지도록 했음.
     */
    fun finishSession(
        stats: RunningStats,
        onSuccess: ((String) -> Unit)? = null
    ) {
        // 결과 화면에서 쓸 수 있도록 무조건 먼저 저장
        lastStats = stats

        val id = sessionId ?: return  // 서버 세션이 아직 없으면 여기까지

        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                repository.finishSession(id, stats)
                onSuccess?.invoke(id)
            } catch (e: Exception) {
                errorMessage = e.message ?: "세션 종료/저장에 실패했습니다."
            } finally {
                isLoading = false
            }
        }
    }
}
