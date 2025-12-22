# Kotlin 파일별 동작 알고리즘 설명서 (발표용)

> 목적: 발표자가 **프론트 코드가 어떤 알고리즘/흐름으로 동작하는지** 빠르게 파악할 수 있도록, **파일 단위**로 핵심 로직을 요약한 문서입니다.

---

## 1) 앱 진입/네비게이션

### `MainActivity.kt`
- **역할**: 앱 진입점 + Google Sign-In 초기화 + Compose 시작.
- **알고리즘**:
    1. Firebase Auth 초기화
    2. Google Sign-In 옵션 구성
    3. `setContent { AppNavGraph(...) }`로 화면 시작
    4. 구글 로그인 결과 콜백을 등록하고 성공 시 Firebase 인증 처리

### `navigation/NavGraph.kt`
- **역할**: 모든 화면 이동 경로 정의.
- **알고리즘**:
    1. `NavHost`에 라우트 등록
    2. 화면 전환 시 필요한 ViewModel 생성 (`AuthViewModel`, `RunningViewModel`)
    3. 화면별 콜백을 통해 다음 화면으로 이동
    4. 러닝 화면(`running`)과 코스/활동 화면 등 주요 진입 흐름 연결

---

## 2) 인증/회원가입 흐름

### `ui/AuthViewModel.kt`
- **역할**: 로그인/회원가입/온보딩 단계 상태 관리.
- **알고리즘(회원가입)**:
    1. 사용자 입력(이메일/비밀번호/닉네임/신체정보/목적/경험)을 StateFlow에 저장
    2. `signup()` 실행 시 입력값 검증
    3. `AuthRepository.signup()` 호출 → 사용자 UUID 확보
    4. UUID 기반으로 `updateBody()` → `updatePurpose()` → `updateExperience()` 순차 호출
    5. 성공 시 `signupCompleted = true`로 UI 전환 유도
- **알고리즘(로그인)**:
    1. `login()` 호출 → `AuthRepository.login()`
    2. 성공 시 `currentUserUuid` 저장
    3. 프로필 조회로 Health Connect 동의 여부 확인
    4. `loginCompleted = true`로 UI 전환 유도

### `ui/screen/LoginScreen.kt`
- **역할**: 로그인 입력 + Health Connect 동의 연동.
- **알고리즘**:
    1. 이메일/비밀번호 입력 → `AuthViewModel.login()`
    2. 로그인 성공 시 Health Connect 권한 확인
    3. 권한이 있으면 헬스 데이터 동기화 → 다음 화면 이동

### `ui/screen/RegisterScreen.kt`
- **역할**: 회원가입 입력 및 중복 체크.
- **알고리즘**:
    1. 이메일/닉네임 중복 체크 실행
    2. Health Connect 동의 체크
    3. 모든 조건 만족 시 `AuthViewModel.signup()` 호출
    4. 완료되면 감사 화면으로 이동

### `ui/screen/UserInfoScreen.kt`
- **알고리즘**: 키/체중 입력 → ViewModel에 저장 → 다음 화면 이동

### `ui/screen/GoalSelectionScreen.kt`
- **알고리즘**: 선택한 목적을 코드로 매핑 → ViewModel 저장 → 다음 화면 이동

### `ui/screen/ExperienceScreen.kt`
- **알고리즘**: 경험값을 ViewModel에 저장 → 회원가입 화면으로 이동

---

## 3) 러닝 세션(시작~종료)

### `ui/RunningViewModel.kt`
- **역할**: 러닝 세션 전체 로직 및 상태 관리.
- **알고리즘(세션 시작)**:
    1. `startSession(userUuid)` 호출
    2. 서버에 세션 시작 요청
    3. `sessionId` 저장 및 위치 추적 초기화
- **알고리즘(위치 추적)**:
    1. 새 GPS 좌표 수신 → `onNewLocation()`
    2. 직전 좌표와 거리 계산
    3. 3m 이상 이동 시 누적 거리 증가
    4. 경로/고도/GPX 포인트 상태 업데이트
- **알고리즘(세션 종료)**:
    1. `finishSession(stats, userUuid)` 호출
    2. GPS 로그/목표 달성 여부 포함해 서버로 종료 요청
    3. 종료 후 결과 조회 → 결과 화면 표시

### `ui/screen/RunningScreen.kt`
- **알고리즘**:
    1. 홈 대시보드 API로 오늘의 플랜 조회
    2. Start 클릭 시 세션 시작 + 카운트다운 전환
    3. 러닝 종료 시 결과 화면으로 이동

### `ui/screen/ActiveRunningScreen.kt`
- **알고리즘**:
    1. 타이머 시작(1초마다 증가)
    2. Health Connect에서 심박 데이터 읽기
    3. 지도에서 실시간 위치 표시
    4. 종료 버튼 길게 누르면 `RunningViewModel.finishSession()` 호출

### `ui/screen/RunningResultScreen.kt`
- **알고리즘**:
    1. 세션 결과 데이터를 시각적으로 요약
    2. 목표 대비 거리/시간/페이스 차이를 계산해 표시

### `ui/screen/RunningFeedbackScreen.kt`
- **알고리즘**:
    1. 별점/통증부위/난이도/코멘트 입력
    2. 이전 피드백 존재 시 자동 불러오기
    3. 제출 버튼 클릭 시 `RunningViewModel.submitFeedback()` 호출

---

## 4) 지도/위치

### `ui/map/RunningMapSection.kt`
- **알고리즘**:
    1. 위치 권한 확인/요청
    2. 네이버 지도 초기화
    3. 현재 위치 기반 카메라 이동 + 마커 표시
    4. 코스 경로가 있으면 Polyline 표시

### `ui/map/MapViewUtils.kt`
- **알고리즘**:
    1. Compose 생명주기와 MapView 생명주기 동기화
    2. 화면 전환 시 지도 리소스 누수 방지

---

## 5) 활동 통계

### `ui/screen/active/ActivityViewModel.kt`
- **알고리즘**:
    1. 월/연/전체 활동 통계 API 호출
    2. 응답 데이터를 UI에 맞는 통계 구조로 변환
    3. 그래프/카드 표시용 요약값 계산

### `ui/screen/active/ActiveScreen.kt`
- **알고리즘**:
    1. ViewModel에서 통계 데이터 로드
    2. 선택 기간에 맞는 그래프 표시
    3. 컨디션 상세 화면으로 이동

### `ui/screen/active/ConditionDetailScreen.kt`
- **알고리즘**: 컨디션 레벨 및 분석 결과를 시각화(현재는 더미 리스트)

---

## 6) 코스(저장/조회)

### `RunningViewModel.saveCourseFromLastRun()`
- **알고리즘**:
    1. 최근 러닝 기록의 GPS 로그 확보
    2. `GpxManager`로 GPX 파일 생성
    3. Base64 인코딩 후 서버에 전송
    4. 성공 시 코스 목록 다시 로드

### `ui/screen/course/*`
- **알고리즘**:
    - 최근 기록, 인기 코스, 내 코스를 서버에서 받아 화면에 표시

---

## 7) 헬스 데이터 연동

### `data/health/HealthConnectManager.kt`
- **알고리즘**:
    1. Health Connect SDK로 걸음수/심박/수면/HRV 조회
    2. 데이터가 없으면 0 처리
    3. ViewModel 또는 UI에서 필요 시 호출

### `data/repository/HealthRepository.kt`
- **알고리즘**: 헬스 데이터를 서버 API로 전송

---

## 8) 네트워크/Repository

### `data/remote/ApiClient.kt`
- **알고리즘**:
    1. Retrofit + Moshi 설정
    2. 공통 로그/헤더 인터셉터 설정
    3. 각 API 인터페이스 인스턴스 제공

### `data/repository/*`
- **알고리즘**:
    - 각 Repository는 ViewModel과 Retrofit 사이에서
      **예외 처리 + 결과 래핑**을 담당

---

## 9) Util/Domain

### `util/GpxManager.kt`
- **알고리즘**:
    1. GPS 좌표 리스트 → GPX XML 문자열 변환
    2. Base64 인코딩 제공

### `domain/model/RunningStatsMapper.kt`
- **알고리즘**:
    1. 서버 응답의 다양한 필드를 통합
    2. 표준화된 `RunningStats`로 변환

---

## 10) 발표용 핵심 요약 (슬라이드 한 장 버전)

- **MVVM 구조**: 화면 → ViewModel → Repository → API
- **러닝 세션 알고리즘**: 시작 → GPS 누적 → 종료 → 결과 조회
- **회원가입 알고리즘**: 유저 생성 → 신체정보 → 목적 → 경험 순차 저장
- **코스 저장 알고리즘**: GPS 로그 → GPX 생성 → 서버 저장
- **헬스 연동 알고리즘**: Health Connect → 데이터 수집 → 서버 업로드

---

필요 시 특정 파일의 상세 코드와 연결 지점은 `RunnersHigh/README.md`를 함께 참고하세요.
