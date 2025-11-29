package com.example.runnershigh.data.repository

import com.example.runnershigh.data.remote.dto.AuthApi
import com.example.runnershigh.data.remote.dto.SignupRequest
import com.example.runnershigh.data.remote.dto.SignupResponse
import com.example.runnershigh.data.remote.dto.LoginRequest
import com.example.runnershigh.data.remote.dto.LoginResponse
import com.example.runnershigh.data.remote.dto.BodyUpdateRequest
import com.example.runnershigh.data.remote.dto.UpdatePurposeRequest
import com.example.runnershigh.data.remote.dto.UpdateExperienceRequest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


// -------------------- 신장·체중 업데이트 결과 --------------------
sealed class BodyUpdateResult {
    data class Success(val message: String) : BodyUpdateResult()
    data class NetworkError(val message: String) : BodyUpdateResult()
}

// -------------------- 이메일 중복 체크 결과 --------------------
sealed class EmailCheckResult {
    data class Available(val message: String) : EmailCheckResult()
    data class Unavailable(val message: String) : EmailCheckResult()
    data class NetworkError(val message: String) : EmailCheckResult()
}

// -------------------- username 중복 체크 결과 --------------------
sealed class UsernameCheckResult {
    data class Available(val message: String) : UsernameCheckResult()
    data class Unavailable(val message: String) : UsernameCheckResult()
    data class NetworkError(val message: String) : UsernameCheckResult()
}


// 회원가입결과 타입
sealed class SignupResult {
    data class Success(
        val userUuid: String,
        val email: String,
        val username: String
    ) : SignupResult()

    // 이메일 / 닉네임 중복 등 비즈니스 에러
    data class BusinessError(val message: String) : SignupResult()

    // 네트워크 / 예외 등
    data class NetworkError(val message: String) : SignupResult()
}


// -------------------- 로그인 결과 --------------------
sealed class LoginResult {
    data class Success(
        val userUuid: String,
        val username: String
    ) : LoginResult()

    data class BusinessError(val message: String) : LoginResult()
    data class NetworkError(val message: String) : LoginResult()
}


class AuthRepository(
    private val authApi: AuthApi
) {

    companion object {
        // 🔥 여기만 true 로 두면 서버 없이 로컬 테스트 모드
        private const val USE_FAKE_API = true
    }

    // -------------------- 회원가입 --------------------
    suspend fun signup(
        email: String,
        password: String,
        username: String
    ): SignupResult = withContext(Dispatchers.IO) {

        // 🔥 FAKE 모드: 서버 안 타고 바로 성공 처리
        if (USE_FAKE_API) {
            return@withContext SignupResult.Success(
                userUuid = "fake-user-uuid-1234",
                email = email,
                username = username
            )
        }

        // 🔽 이하 실제 서버 연동 코드 (그대로 유지)
        try {
            val response = authApi.signup(
                SignupRequest(
                    email = email,
                    password = password,
                    username = username
                )
            )

            if (!response.isSuccessful) {
                // HTTP 코드가 200대가 아니면 여기로
                return@withContext SignupResult.NetworkError(
                    "Server error: ${response.code()}"
                )
            }

            val body: SignupResponse? = response.body()

            if (body == null) {
                return@withContext SignupResult.NetworkError(
                    "Empty response from server"
                )
            }

            // ✅ 1) 정상 회원가입인 경우
            if (!body.user_uuid.isNullOrBlank()) {
                return@withContext SignupResult.Success(
                    userUuid = body.user_uuid,
                    email = body.email ?: email,
                    username = body.username ?: username
                )
            }

            // ✅ 2) 이메일/닉네임 중복 등 비즈니스 에러
            if (!body.error.isNullOrBlank()) {
                return@withContext SignupResult.BusinessError(
                    message = body.error
                )
            }

            // ✅ 3) 예상치 못한 응답
            SignupResult.NetworkError(
                "Unexpected response from server"
            )
        } catch (e: HttpException) {
            SignupResult.NetworkError("HttpException: ${e.code()}")
        } catch (e: IOException) {
            SignupResult.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            SignupResult.NetworkError("Unknown error: ${e.message}")
        }
    }


    // ---------- 신장·체중 업데이트 ----------
    suspend fun updateBody(
        userUuid: String,
        height: Int,
        weight: Int
    ): BodyUpdateResult = withContext(Dispatchers.IO) {

        // 🔥 FAKE 모드
        if (USE_FAKE_API) {
            return@withContext BodyUpdateResult.Success(
                "테스트 모드: 프로필이 성공적으로 업데이트 되었습니다."
            )
        }

        // 🔽 실제 서버 연동 코드
        try {
            val response = authApi.updateBody(
                BodyUpdateRequest(
                    user_uuid = userUuid,
                    height = height,
                    weight = weight
                )
            )

            if (!response.isSuccessful) {
                return@withContext BodyUpdateResult.NetworkError(
                    "Server error: ${response.code()}"
                )
            }

            val body = response.body()
            val msg = body?.message ?: "Profile updated successfully"

            BodyUpdateResult.Success(msg)
        } catch (e: HttpException) {
            BodyUpdateResult.NetworkError("HttpException: ${e.code()}")
        } catch (e: IOException) {
            BodyUpdateResult.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            BodyUpdateResult.NetworkError("Unknown error: ${e.message}")
        }
    }


    // ---------- 러닝 목적 업데이트 ----------
    suspend fun updatePurpose(userUuid: String, purpose: List<String>): Boolean =
        withContext(Dispatchers.IO) {

            // 🔥 FAKE 모드
            if (USE_FAKE_API) {
                return@withContext true
            }

            // 🔽 실제 서버 연동 코드
            try {
                val request = UpdatePurposeRequest(
                    user_uuid = userUuid,
                    runningPurpose = purpose
                )

                val response = authApi.updatePurpose(userUuid, request)

                return@withContext response.isSuccessful
            } catch (e: Exception) {
                return@withContext false
            }
        }


    // ---------- 러닝 경험 업데이트 ----------
    suspend fun updateExperience(
        userUuid: String,
        experienceCode: String
    ): Boolean = withContext(Dispatchers.IO) {

        // 🔥 FAKE 모드
        if (USE_FAKE_API) {
            return@withContext true
        }

        // 🔽 실제 서버 연동 코드
        try {
            val response = authApi.updateExperience(
                UpdateExperienceRequest(
                    user_uuid = userUuid,
                    runningExperience = experienceCode
                )
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }


    // ---------- 이메일 체크 ----------
    suspend fun checkEmail(email: String): EmailCheckResult =
        withContext(Dispatchers.IO) {

            // 🔥 FAKE 모드
            if (USE_FAKE_API) {
                // 항상 사용 가능하다고 가정
                return@withContext EmailCheckResult.Available(
                    "테스트 모드: 사용 가능한 이메일입니다."
                )
            }

            // 🔽 실제 서버 연동 코드
            try {
                val response = authApi.checkEmail(email)

                if (!response.isSuccessful) {
                    return@withContext EmailCheckResult.NetworkError(
                        "Server error: ${response.code()}"
                    )
                }

                val body = response.body()
                    ?: return@withContext EmailCheckResult.NetworkError(
                        "Empty response from server"
                    )

                if (body.available) {
                    EmailCheckResult.Available(body.message)
                } else {
                    EmailCheckResult.Unavailable(body.message)
                }
            } catch (e: HttpException) {
                EmailCheckResult.NetworkError("HttpException: ${e.code()}")
            } catch (e: IOException) {
                EmailCheckResult.NetworkError("Network error: ${e.message}")
            } catch (e: Exception) {
                EmailCheckResult.NetworkError("Unknown error: ${e.message}")
            }
        }


    // ---------- username 중복 체크 ----------
    suspend fun checkUsername(username: String): UsernameCheckResult =
        withContext(Dispatchers.IO) {

            // 🔥 FAKE 모드
            if (USE_FAKE_API) {
                return@withContext UsernameCheckResult.Available(
                    "테스트 모드: 사용 가능한 닉네임입니다."
                )
            }

            // 🔽 실제 서버 연동 코드
            try {
                val response = authApi.checkUsername(username)

                if (!response.isSuccessful) {
                    return@withContext UsernameCheckResult.NetworkError(
                        "Server error: ${response.code()}"
                    )
                }

                val body = response.body()
                    ?: return@withContext UsernameCheckResult.NetworkError(
                        "Empty response from server"
                    )

                if (body.available) {
                    UsernameCheckResult.Available(body.message)
                } else {
                    UsernameCheckResult.Unavailable(body.message)
                }
            } catch (e: HttpException) {
                UsernameCheckResult.NetworkError("HttpException: ${e.code()}")
            } catch (e: IOException) {
                UsernameCheckResult.NetworkError("Network error: ${e.message}")
            } catch (e: Exception) {
                UsernameCheckResult.NetworkError("Unknown error: ${e.message}")
            }
        }


    // -------------------- 로그인 --------------------
    suspend fun login(
        email: String,
        password: String
    ): LoginResult = withContext(Dispatchers.IO) {

        // 🔥 FAKE 모드
        if (USE_FAKE_API) {
            return@withContext LoginResult.Success(
                userUuid = "fake-user-uuid-1234",
                username = "테스트러너"
            )
        }

        // 🔽 실제 서버 연동 코드
        try {
            val response = authApi.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            if (!response.isSuccessful) {
                return@withContext LoginResult.NetworkError(
                    "Server error: ${response.code()}"
                )
            }

            val body: LoginResponse? = response.body()

            if (body == null) {
                return@withContext LoginResult.NetworkError(
                    "Empty response from server"
                )
            }

            // 성공: UUID가 존재할 때
            if (!body.user_uuid.isNullOrBlank()) {
                return@withContext LoginResult.Success(
                    userUuid = body.user_uuid,
                    username = body.username ?: ""
                )
            }

            // 비밀번호 틀림 / 이메일 없음 등
            if (!body.error.isNullOrBlank()) {
                return@withContext LoginResult.BusinessError(
                    message = body.error
                )
            }

            LoginResult.NetworkError("Unexpected response from server")
        } catch (e: HttpException) {
            LoginResult.NetworkError("HttpException: ${e.code()}")
        } catch (e: IOException) {
            LoginResult.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            LoginResult.NetworkError("Unknown error: ${e.message}")
        }
    }
}
