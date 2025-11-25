package com.example.runnershigh.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnershigh.data.remote.ApiClient
import com.example.runnershigh.data.repository.AuthRepository
import com.example.runnershigh.data.repository.SignupResult
import com.example.runnershigh.data.repository.LoginResult
import com.example.runnershigh.data.repository.EmailCheckResult
import com.example.runnershigh.data.repository.UsernameCheckResult
import com.example.runnershigh.data.repository.BodyUpdateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// -------------------- 회원 가입 UI 상태 --------------------
data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val signupCompleted: Boolean = false,

    // ✅ 이메일 중복 체크 관련
    val isCheckingEmail: Boolean = false,
    val isEmailAvailable: Boolean? = null,   // null: 아직 확인 안 함
    val emailCheckMessage: String? = null,

    // ✅ username(닉네임) 중복 체크
    val isCheckingUsername: Boolean = false,
    val isUsernameAvailable: Boolean? = null,
    val usernameCheckMessage: String? = null
)

// -------------------- 로그인 UI 상태 --------------------
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val loginCompleted: Boolean = false
)

// -------------------- 신장·체중 UI 상태 (온보딩 앞 단계에서 사용) --------------------
data class BodyUiState(
    val height: String = "",    // cm, 문자열로 입력 받음
    val weight: String = "",    // kg, 문자열로 입력 받음
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository(ApiClient.authApi)

    // 현재 가입/로그인한 유저 UUID (회원가입 성공 시 세팅, 로그인 성공 시 세팅)
    private var currentUserUuid: String? = null

    // 회원가입 UI 상태
    private val _signupUiState = MutableStateFlow(SignupUiState())
    val signupUiState: StateFlow<SignupUiState> = _signupUiState

    // 로그인 UI 상태
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    // 신장·체중 UI 상태 (UserInfoScreen 에서 사용)
    private val _bodyUiState = MutableStateFlow(BodyUiState())
    val bodyUiState: StateFlow<BodyUiState> = _bodyUiState

    // ----------------------------------------------------------------------
    //  신장·체중 입력 (UserInfoScreen) - 이 단계에서는 서버 호출 X, 값만 보관
    // ----------------------------------------------------------------------
    fun onBodyHeightChange(value: String) {
        _bodyUiState.update {
            it.copy(
                height = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onBodyWeightChange(value: String) {
        _bodyUiState.update {
            it.copy(
                weight = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // ----------------------------------------------------------------------
    //  회원가입 입력 및 이메일/닉네임 중복 체크
    // ----------------------------------------------------------------------
    fun onEmailChange(value: String) {
        _signupUiState.update {
            it.copy(
                email = value,
                errorMessage = null,
                successMessage = null,
                isEmailAvailable = null,      // 이메일이 바뀌면 다시 확인해야 함
                emailCheckMessage = null
            )
        }
    }

    fun checkEmail() {
        val email = _signupUiState.value.email

        if (email.isBlank()) {
            _signupUiState.update {
                it.copy(emailCheckMessage = "이메일을 먼저 입력해주세요.")
            }
            return
        }

        viewModelScope.launch {
            _signupUiState.update {
                it.copy(
                    isCheckingEmail = true,
                    emailCheckMessage = null
                )
            }

            when (val result = repository.checkEmail(email)) {
                is EmailCheckResult.Available -> {
                    _signupUiState.update {
                        it.copy(
                            isCheckingEmail = false,
                            isEmailAvailable = true,
                            emailCheckMessage = result.message
                        )
                    }
                }

                is EmailCheckResult.Unavailable -> {
                    _signupUiState.update {
                        it.copy(
                            isCheckingEmail = false,
                            isEmailAvailable = false,
                            emailCheckMessage = result.message
                        )
                    }
                }

                is EmailCheckResult.NetworkError -> {
                    _signupUiState.update {
                        it.copy(
                            isCheckingEmail = false,
                            isEmailAvailable = false,
                            emailCheckMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun checkUsername() {
        val username = _signupUiState.value.username

        if (username.isBlank()) {
            _signupUiState.update {
                it.copy(usernameCheckMessage = "닉네임을 먼저 입력해주세요.")
            }
            return
        }

        viewModelScope.launch {
            _signupUiState.update {
                it.copy(
                    isCheckingUsername = true,
                    usernameCheckMessage = null
                )
            }

            when (val result = repository.checkUsername(username)) {
                is UsernameCheckResult.Available -> {
                    _signupUiState.update {
                        it.copy(
                            isCheckingUsername = false,
                            isUsernameAvailable = true,
                            usernameCheckMessage = result.message
                        )
                    }
                }

                is UsernameCheckResult.Unavailable -> {
                    _signupUiState.update {
                        it.copy(
                            isCheckingUsername = false,
                            isUsernameAvailable = false,
                            usernameCheckMessage = result.message
                        )
                    }
                }

                is UsernameCheckResult.NetworkError -> {
                    _signupUiState.update {
                        it.copy(
                            isCheckingUsername = false,
                            isUsernameAvailable = false,
                            usernameCheckMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onPasswordChange(value: String) {
        _signupUiState.update {
            it.copy(
                password = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onUsernameChange(value: String) {
        _signupUiState.update {
            it.copy(
                username = value,
                errorMessage = null,
                successMessage = null,
                isUsernameAvailable = null,
                usernameCheckMessage = null
            )
        }
    }

    // ----------------------------------------------------------------------
    //  회원가입 실행
    //
    //  온보딩 흐름:
    //   1) UserInfoScreen 에서 height/weight 입력 (BodyUiState 에 저장)
    //   2) (목표/경험 화면도 같은 패턴으로 ViewModel 에 값만 저장 예정)
    //   3) RegisterScreen 에서 email/password/username 입력 + 중복체크
    //   4) 최종 Register 버튼 클릭 시
    //      → ① signup_api 호출 (user_uuid 생성)
    //      → ② update_body_api 호출 (height/weight + user_uuid)
    // ----------------------------------------------------------------------
    fun signup() {
        val signup = _signupUiState.value
        val body = _bodyUiState.value

        // 기본 입력값 검증
        if (signup.email.isBlank() || signup.password.isBlank() || signup.username.isBlank()) {
            _signupUiState.update {
                it.copy(errorMessage = "모든 필드를 입력해주세요.")
            }
            return
        }

        // 이메일/닉네임 중복 체크를 강제하고 싶다면 아래 주석을 해제해도 됨
        if (signup.isEmailAvailable != true) {
            _signupUiState.update {
                it.copy(errorMessage = "이메일 중복을 먼저 확인해주세요.")
            }
            return
        }

        if (signup.isUsernameAvailable != true) {
            _signupUiState.update {
                it.copy(errorMessage = "닉네임 중복을 먼저 확인해주세요.")
            }
            return
        }

        // 신장·체중 정보가 온보딩 앞 단계에서 들어왔는지 확인
        val heightInt = body.height.toIntOrNull()
        val weightInt = body.weight.toIntOrNull()

        if (heightInt == null || weightInt == null) {
            _signupUiState.update {
                it.copy(errorMessage = "신장과 체중 정보를 먼저 입력해주세요.")
            }
            return
        }

        viewModelScope.launch {
            _signupUiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            // 1) 회원가입 (user_uuid 생성)
            when (val signupResult = repository.signup(
                email = signup.email,
                password = signup.password,
                username = signup.username
            )) {
                is SignupResult.Success -> {
                    // signup 성공 → UUID 확보
                    currentUserUuid = signupResult.userUuid

                    // 2) 신장·체중 업데이트
                    when (val bodyResult = repository.updateBody(
                        userUuid = signupResult.userUuid,
                        height = heightInt,
                        weight = weightInt
                    )) {
                        is BodyUpdateResult.Success -> {
                            _signupUiState.update {
                                it.copy(
                                    isLoading = false,
                                    signupCompleted = true,
                                    successMessage = "회원가입 및 프로필 설정이 완료되었습니다."
                                )
                            }
                            // 필요하면 여기서 BodyUiState 성공 메시지도 세팅 가능
                            _bodyUiState.update { bodyState ->
                                bodyState.copy(
                                    errorMessage = null,
                                    successMessage = "프로필이 저장되었습니다."
                                )
                            }
                        }

                        is BodyUpdateResult.NetworkError -> {
                            _signupUiState.update {
                                it.copy(
                                    isLoading = false,
                                    signupCompleted = false,
                                    errorMessage = "프로필 저장 중 오류가 발생했습니다: ${bodyResult.message}"
                                )
                            }
                        }
                    }
                }

                is SignupResult.BusinessError -> {
                    _signupUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = signupResult.message
                        )
                    }
                }

                is SignupResult.NetworkError -> {
                    _signupUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = signupResult.message
                        )
                    }
                }
            }
        }
    }

    fun consumeSignupCompletedFlag() {
        _signupUiState.update { it.copy(signupCompleted = false) }
    }

    // ----------------------------------------------------------------------
    //  로그인
    // ----------------------------------------------------------------------
    fun onLoginEmailChange(value: String) {
        _loginUiState.update {
            it.copy(email = value, errorMessage = null, successMessage = null)
        }
    }

    fun onLoginPasswordChange(value: String) {
        _loginUiState.update {
            it.copy(password = value, errorMessage = null, successMessage = null)
        }
    }

    fun login() {
        val current = _loginUiState.value

        if (current.email.isBlank() || current.password.isBlank()) {
            _loginUiState.update {
                it.copy(errorMessage = "이메일과 비밀번호를 입력해주세요.")
            }
            return
        }

        viewModelScope.launch {
            _loginUiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }

            when (val result = repository.login(
                email = current.email,
                password = current.password
            )) {
                is LoginResult.Success -> {
                    // 로그인 성공 시 현재 유저 UUID 저장
                    currentUserUuid = result.userUuid

                    _loginUiState.update {
                        it.copy(
                            isLoading = false,
                            loginCompleted = true,
                            successMessage = "로그인에 성공했습니다."
                        )
                    }
                }

                is LoginResult.BusinessError -> {
                    _loginUiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }

                is LoginResult.NetworkError -> {
                    _loginUiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun consumeLoginCompletedFlag() {
        _loginUiState.update { it.copy(loginCompleted = false) }
    }
}
