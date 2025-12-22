`gpxapi/myapplication`에 있던 코틀린 파일들은 RunnersHigh 앱의 ViewModel · Repository · UI 흐름으로 모두 옮겨 놓았습니다. 이제 빌드에 gpxapi 모듈이 관여하지 않으므로, 디렉터리를 삭제해도 기능이 유지됩니다.

## 기능별 통합 위치
- **좌표/시간/고도 모델**: `LocationPoint`, `LocationInfo` → `app/src/main/java/com/example/runnershigh/domain/model/GpxLocationPoint.kt` 및 `RunningLocationState`에서 사용.
- **GPX 생성 & Base64 인코딩**: `GpxManager` → `app/src/main/java/com/example/runnershigh/util/GpxManager.kt`.
- **러닝 중 좌표 누적/거리·고도 계산**: `MainActivity` 위치 업데이트 로직 → `app/src/main/java/com/example/runnershigh/ui/RunningViewModel.kt`의 `onNewLocation`.
- **러닝 종료 후 코스 업로드**: `RunDataModel`, `RunRecordService`, `RunDataUploader` → `RunningViewModel.finishSession`이 `RunningRepository.createRunningCourse`를 호출해 GPX XML(Base64)·누적거리·경로를 함께 전송.
- **코스 조회/선택 UI**: `FirstFragment` 등 → `navigation/NavGraph` + `ui/screen/course/*` 흐름으로 통합. 선택한 코스는 `RunningScreen`과 `ActiveRunningScreen` 지도에 바로 그려집니다.

## 동작 흐름 체크리스트
1. **러닝 시작**: `RunningScreen`의 Start 버튼 → `RunningViewModel.startSession` + `startTracking`으로 상태 초기화 및 추적 시작.
2. **GPS 수집**: 위치 콜백에서 `RunningViewModel.onNewLocation` 호출 → 지도 경로, 누적 거리/고도, GPX 포인트(UTC 타임스탬프 포함)를 모두 저장.
3. **러닝 종료**: `RunningViewModel.finishSession`이 세션 종료 API 호출 후 `_lastRunForCourse`에 GPX 포인트와 통계를 보존.
4. **코스 등록**: `RegisterCourseScreen`에서 입력한 **실제 코스 이름**과 로그인된 **userUuid**를 `saveCourseFromLastRun`에 전달 → GPX XML을 Base64로 변환해 파일 이름과 함께 업로드.
5. **코스 불러오기/선택**: `MyCourseScreen` 진입 시 `loadUserCourses`로 백엔드 목록을 불러오고, 선택 시 `selectCourse`가 지도에 코스 경로를 그린 채 러닝 화면으로 이동.
6. **피드백 후 새 코스 생성**: 러닝 종료 후 피드백 화면의 "새 코스 생성" 버튼이 바로 등록 폼으로 이동하도록 연결.

## 남아 있는 gpxapi 디렉터리
- 현재 `gpxapi/`는 샘플 빌드 산출물만 포함하며 `.gitignore`에 추가되어 있습니다. 실제 앱 동작은 모두 `RunnersHigh/app/src/main/java/com/example/runnershigh` 아래 코드만 사용합니다.