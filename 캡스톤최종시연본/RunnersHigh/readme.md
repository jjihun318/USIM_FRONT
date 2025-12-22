# Runner’s High 프론트엔드/백엔드 연동 문서 (Kotlin / Jetpack Compose)

> 이 문서는 프론트 코드 구조, 각 Kotlin 파일의 역할, 프론트–백엔드 호출 흐름, 기능별 동작 알고리즘을
> 백엔드 개발자가 바로 이해할 수 있도록 한국어로 상세하게 정리한 README입니다.

---

## 1) 프로젝트 개요

Runner’s High는 러닝 세션을 기록하고 활동 통계, 피드백, 코스, 배지, 헬스 데이터를 통합해 보여주는 Android 앱입니다.

- 프론트엔드: Kotlin + Jetpack Compose + MVVM
- 백엔드 호출: Retrofit + Cloud Run API 엔드포인트 (ApiEndpoints.kt)
- 지도: 네이버 지도 SDK
- 헬스 데이터: Health Connect

---

## 2) 프론트/백엔드 전체 흐름 개요

대부분의 화면은 아래 구조를 기준으로 동작합니다.

[UI(Screen/Compose)]  
↓ (상태/이벤트)  
[ViewModel]  
↓ (도메인 상태 관리 + 로직)  
[Repository]  
↓ (Retrofit 호출)  
[ApiClient + Api 인터페이스]  
↓ (Cloud Run API 호출)  
[백엔드]  
↓ (Response DTO)  
[ViewModel / Screen에 결과 반영]

### 핵심 공통 구조

- ApiClient.kt  
  Retrofit / OkHttp / Moshi 설정 및 API 인스턴스 생성

- ApiEndpoints.kt  
  백엔드 엔드포인트 URL 정의

- data/remote/dto  
  Request / Response 데이터 모델

- data/repository  
  Retrofit 호출을 감싸는 중간 계층 (예외 / 로딩 처리)

- ui  
  ViewModel + Compose UI

---

## 3) 기능별 프론트–백엔드 연동 흐름

### 3.1 로그인 / 회원가입 흐름

관련 파일
- ui/AuthViewModel.kt
- data/repository/AuthRepository.kt
- data/remote/dto/AuthApi.kt
- data/remote/ApiEndpoints.kt
- ui/screen/LoginScreen.kt
- ui/screen/RegisterScreen.kt
- ui/screen/UserInfoScreen.kt
- ui/screen/GoalSelectionScreen.kt
- ui/screen/ExperienceScreen.kt

로그인 흐름
1. LoginScreen에서 이메일 / 비밀번호 입력
2. AuthViewModel.login() 호출
3. AuthRepository.login()
4. AuthApi.login()
5. ApiEndpoints.LOGIN_API 호출
6. 성공 시 AuthViewModel.currentUserUuid 저장
7. getProfile 호출로 Health Connect 동의 여부 확인
8. 러닝 메인 화면으로 이동

회원가입 온보딩 흐름
1. UserInfoScreen에서 키 / 체중 입력
2. GoalSelectionScreen에서 러닝 목적 선택
3. ExperienceScreen에서 러닝 경험 선택
4. RegisterScreen에서 이메일 / 비밀번호 / 닉네임 입력
5. AuthViewModel.signup() 호출

회원가입 내부 API 호출 순서
1) signup_api (유저 생성)
2) update_body_api (신체 정보 저장)
3) update_purpose_api (러닝 목적 저장)
4) update_experience_api (러닝 경험 저장)

백엔드 관점 포인트
- 회원가입은 순차 호출 구조
- 앞 단계 실패 시 이후 API 호출 중단
- 로그인 후 Health Connect 동의 여부에 따라 헬스 데이터 동기화 진행

---

### 3.2 러닝 세션 시작 / 진행 / 종료

관련 파일
- ui/RunningViewModel.kt
- data/repository/RunningRepository.kt
- data/remote/dto/RunningApi.kt
- ui/screen/RunningScreen.kt
- ui/screen/ActiveRunningScreen.kt
- ui/screen/RunningResultScreen.kt

세션 시작
1. RunningScreen에서 Start 버튼 클릭
2. RunningViewModel.startSession(userUuid)
3. RunningRepository.startSession()
4. RunningApi.startSession()
5. 응답으로 받은 sessionId 저장
6. startTracking() 호출 → 위치 추적 활성화

세션 진행
- ActiveRunningScreen에서 위치 변화 감지
- RunningViewModel.onNewLocation()
- 직전 좌표와 거리 계산 (3m 이상 이동 시 누적)
- RunningLocationState 업데이트
- 필요 시 uploadGpsPoint API 호출

세션 종료
1. finishSession(stats, userUuid)
2. RunningRepository.finishSession()
3. RunningApi.finishSession()
4. 종료 결과 저장
5. loadRunningResult() 호출
6. RunningResultScreen에서 결과 표시

백엔드 관점 포인트
- finishSession 요청에 거리, 시간, 심박, 고도, 케이던스, GPS 로그 포함
- 결과 응답에 배지 및 경험치 정보 포함

---

### 3.3 러닝 결과 / 피드백

관련 파일
- ui/screen/RunningResultScreen.kt
- ui/screen/RunningFeedbackScreen.kt

흐름
1. 러닝 결과 화면에서 피드백 입력
2. RunningViewModel.submitFeedback()
3. RunningApi.submitFeedback()
4. CREATE_FEEDBACK_API 호출
5. 제출된 피드백은 getSubmittedFeedback으로 조회 가능

---

### 3.4 활동 통계 (Active)

관련 파일
- ui/screen/active/ActiveScreen.kt
- ui/screen/active/ActivityViewModel.kt
- data/remote/api/ActivityApi.kt

흐름
1. ActivityViewModel.loadActivityData()
2. 월 / 연 / 전체 통계 API 호출
3. ActivityUiState에 결과 저장
4. ActiveScreen에서 카드 / 그래프 렌더링
5. ConditionDetailScreen으로 상세 분석 이동 가능

---

### 3.5 코스 저장 / 조회

관련 파일
- ui/screen/course/*
- RunningViewModel.saveCourseFromLastRun()
- RunningRepository.createRunningCourse()

흐름
1. 최근 러닝 기록에서 GPS 로그 추출
2. GpxManager로 GPX XML 생성
3. Base64 인코딩
4. create-running-course API 호출
5. get-running-courses API로 내 코스 목록 조회

---

### 3.6 배지 / 레벨 / 미션

관련 파일
- ui/screen/level/*
- data/remote/dto/Badge.kt
- data/remote/dto/Mission.kt
- data/remote/dto/UserLevel.kt
- data/remote/dto/UserService.kt

흐름
- 러닝 종료 시 배지 및 경험치 획득
- UserService API를 통해 레벨 / 배지 / 미션 조회

---

### 3.7 헬스 데이터 연동

관련 파일
- data/health/HealthConnectManager.kt
- data/repository/HealthRepository.kt
- data/remote/dto/HealthApi.kt

흐름
1. 로그인 또는 회원가입 시 Health Connect 권한 동의
2. HealthConnectManager가 걸음수, 심박, 수면, HRV 데이터 조회
3. HealthRepository.syncHealthData()
4. SEND_HEALTH_DATA_API 호출

---

## 4) 프론트 코드 디렉토리 & Kotlin 파일 역할

(app/src/main/java/com/example/runnershigh 기준)

### 루트
- MainActivity.kt : 앱 진입점
- OnboardingActivity.kt : Health Connect 온보딩
- PermissionsRationaleActivity.kt : 권한 안내

### Navigation
- navigation/NavGraph.kt : 화면 이동 및 ViewModel 공유

### ViewModel
- ui/AuthViewModel.kt
- ui/RunningViewModel.kt

### Theme
- ui/theme/Theme.kt
- ui/theme/Color.kt
- ui/theme/Type.kt

### Network
- data/remote/ApiClient.kt
- data/remote/ApiEndpoints.kt

### Repository
- AuthRepository.kt
- RunningRepository.kt
- HealthRepository.kt
- NaverGeocodeRepository.kt

### DTO / Domain / Util / Test
(기존 문서에 정의된 모든 파일 그대로 포함)

---

## 5) 백엔드 개발자를 위한 API 호출 위치 요약

- AuthViewModel.login / signup
- RunningViewModel.startSession / finishSession / uploadGpsPoint
- ActivityViewModel.loadActivityData
- RunningViewModel.saveCourseFromLastRun
- RunningViewModel.submitFeedback

---

## 6) 데이터 → UI 반영 예시

러닝 결과
finishSession → SessionResultResponse → RunningStatsMapper → RunningResultScreen

활동 통계
ActivityApi → ActivityUiState → ActiveScreen

코스 등록
GPS 로그 → GPX → create-running-course → MyCourseScreen

---

## 7) 엔드포인트 정의

모든 백엔드 엔드포인트는  
data/remote/ApiEndpoints.kt 파일에 정의되어 있습니다.

---

## 8) 요약

- 프론트 구조: Compose UI → ViewModel → Repository → Retrofit API
- ViewModel이 상태 관리
- Repository가 백엔드 호출 책임
- DTO는 화면 표시를 위한 단일 데이터 소스
- 백엔드 개발자는 이 문서를 통해 호출 위치와 데이터 흐름을 즉시 파악할 수 있습니다.
