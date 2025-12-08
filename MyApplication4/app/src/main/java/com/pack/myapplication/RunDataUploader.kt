package com.pack.myapplication.data

import android.util.Log
import com.pack.myapplication.api.RetrofitClient
import com.pack.myapplication.RunRecordRequest // 요청 데이터 모델

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 러닝 기록 데이터를 백엔드 서버에 업로드하는 역할을 담당하는 클래스입니다.
 */
class RunDataUploader {

    // RetrofitClient에 정의된 RunRecordService 인스턴스를 가져옵니다.
    private val apiService = RetrofitClient.runRecordService

    /**
     * 비동기적으로 러닝 기록 데이터를 서버에 업로드합니다.
     *
     * @param data 서버로 전송할 러닝 기록 데이터 (RunRecordRequest 객체)
     * @return Boolean 업로드 성공 여부 (true: 성공, false: 실패)
     */
    suspend fun uploadRunData(data: RunRecordRequest): Boolean {
        // 네트워크 작업은 IO 디스패처에서 실행합니다.
        return withContext(Dispatchers.IO) {
            try {
                // 1. Retrofit 서비스 메서드를 호출하여 API 요청을 실행합니다.
                val response = apiService.uploadRunRecord(data)

                // 2. 응답이 성공적인지 확인합니다 (HTTP 200-299).
                if (response.isSuccessful) {
                    val resultBody = response.body()
                    Log.d("RunDataUploader", "✅ 업로드 성공: Run ID = ${resultBody?.courseId}")
                    // 추가적으로 resultBody?.success 필드를 확인하는 로직을 넣을 수 있습니다.
                    return@withContext true
                } else {
                    // 3. 응답은 받았으나 성공 코드가 아닌 경우 (예: 400, 500)
                    val errorBody = response.errorBody()?.string()
                    Log.e("RunDataUploader", "❌ 업로드 실패 - HTTP Code: ${response.code()}, Error: $errorBody")
                    return@withContext false
                }
            } catch (e: Exception) {
                // 4. 네트워크 연결 오류, 타임아웃 등 요청 자체가 실패한 경우
                Log.e("RunDataUploader", "❌ 네트워크 요청 오류 발생: ${e.message}", e)
                return@withContext false
            }
        }
    }
}