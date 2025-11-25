package com.example.runnershigh.data.remote.dto

/**
 * 러닝 세션 시작 API 응답
 *
 * POST /sessions/start
 * 백엔드에서 새로 생성한 sessionId(or session_uuid)를 내려준다고 가정.
 * 실제 필드 이름은 백엔드와 한 번 맞춰봐야 함!
 */
data class StartSessionResponse(
    // 백엔드가 session_uuid 라고 보내면 이름을 session_uuid 로 바꾸고
    // @SerializedName("session_uuid") 사용해도 됨.
    val sessionId: String
)
