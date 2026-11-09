# 살핌 (Salpim) — 에이전트 개발 규칙

Pleos Connect(AAOS) 상태 인지형 안전 동반 앱. 소개·일정·대회 정보·기획서 매핑은 [README.md](README.md)에 있다.

이 파일이 모든 코딩 에이전트(Claude Code, Codex 등)의 공통 규칙이다. `CLAUDE.md`는 이 파일을 불러오기만 한다. 규칙은 여기에만 쓴다.

표기: **[확인됨]** 공식 문서/공고에서 확인 · **[미확인]** spike로 검증 필요 · **[설계]** 우리가 정한 결정

## 0. 최우선 규칙

- **이 문서와 공식 문서에 없는 Pleos SDK 클래스/메서드 이름을 추측해서 쓰지 않는다.** 불확실하면 인터페이스 뒤에 숨기고 `TODO(spike-Sx)`를 남긴다.
- 문서와 실제 SDK가 다르면 SDK 소스/KDoc이 우선이다.
- 세션 시작 시 이 문서와 `docs/spikes/`를 먼저 읽는다.

## 1. 확인된 플랫폼 사실

- AAOS 기반. 같은 기능이 AAOS에 있으면 표준 API를 쓴다 **[확인됨]**
- Vehicle SDK 그룹 **[확인됨]**: Brake, CarInfo, Door, DrivingMode, EvBattery, HVAC, Light, Odometer, Safety, Seat, SideMirror, Steeringwheel, Tire, TurnSignal, Window, Wiper
  - `vehicle.getXXX()`로 그룹 객체, 시작 시 `vehicle.initialize()`, 종료 시 `vehicle.release()`
  - Safety 공개 API(`CAR_SAFETY`)는 오일 3종 레벨(`OilLevelState`), 파워다운·워셔액·안전벨트·에어백 전개뿐. **경고등(telltale)과 타이어 공기압은 signature 권한이라 서드파티 불가** (S2)
  - Brake 공개 API는 브레이크 **페달** 상태만. **속도·기어·주차브레이크 API는 Vehicle SDK에 없음** → AAOS `CarPropertyManager` 경로 검토 중 (S3)
  - Steeringwheel 값은 핸들각이 아니라 **앞바퀴 조향각** (S3)
- 권한 **[확인됨]**: `pleos.car.permission.CAR_DRIVE`(DrivingMode, Brake, Odometer), `CAR_ENERGY`(EvBattery), `CAR_SAFETY`(Safety, Tire). **쓰는 도메인 권한만 선언**
- 차종별 호환성 다름 → `checkXXXCapability` 계열로 반드시 확인. NaviHelper·GleoAI는 완전 호환. **Fused Location, ADAS는 사용하지 않는다**
- SDK (2026-11-09 공식 문서, S6) **[확인됨]**: 저장소 `https://nexus-playground.pleos.ai/repository/maven-releases/`, `ai.pleos.playground:Vehicle:2.3.1`, `NaviHelper:3.0.5`, `TextToSpeech:3.3.1`, `SpeechToText:3.3.1`, `FusedLocation:1.1.2` 모두 공개. Vehicle Control은 Vehicle SDK의 `setXxx` + `CONTROL_CAR_*`. LLM은 레퍼런스만 있고 설치 가이드 없음 → 사용 안 함
- **미승인 권한을 선언하거나 해당 API를 호출하면 리뷰 반려** **[확인됨]**
- NaviHelper **[확인됨]**: `requestRoute(RouteInfo(...))`로 목적지, POI 검색 결과 API는 없고 `requestNavigationScreen(SearchPlace(keyword))`로 내비 검색 화면을 연다 (S5)
- 주행 제한 **[확인됨]**: `CarUxRestrictionsManager` → `onUxRestrictionsChanged()` → `ActiveRestrictions` 플래그별로 앱이 직접 제한. DO를 선언해도 동일
- 하드웨어 직접 접근 금지, 플랫폼 API만. 주행 중 네트워크 불안정 → 오프라인 우선
- 개발 요건: Kotlin, minSdk 28 (튜토리얼의 26 아님), Compose 사용 가능

## 2. 아키텍처 규칙

```
app → feature/{warning,service,coach,glance} → core/{model,state,signal,ux,data}
```

- `core/model`은 순수 Kotlin, 아무것도 의존하지 않는다.
- **Pleos SDK 호출은 `core/signal`에만** (NaviHelper 어댑터 포함: `core/signal/navi`). SDK 타입은 밖으로 새지 않는다. 나머지는 `core/model` 타입만 본다.
- Android Car API(`android.car`)는 `core/ux`에만. 비자동차 기기에서는 `FEATURE_AUTOMOTIVE` 확인 후 정적 소스로 대체.
- UI: Compose + ViewModel + StateFlow 단방향. 화면은 `UxPolicy`와 `DrivingState`로 렌더링을 결정한다. 파이프라인은 `app/SalpimEngine`.
- 새 로직은 순수 Kotlin으로 작성하고 단위 테스트부터 붙인다. 시간은 신호 timestamp/주입한 clock만 쓴다 (테스트 결정성).
- 시뮬레이션 신호가 활성이면 **항상 `SIMULATED` 배지**를 표시한다. 실제 신호와 섞어 보이게 만들지 않는다.

## 3. 안전/UX 규칙 (어기지 않는다)

1. DRIVING 중: 텍스트 입력 없음, 스크롤/다단계 탐색 없음, 영상·애니메이션 과다 금지, **한 번에 정보 최대 3개**
2. `CarUxRestrictions`가 활성이면 `UxPolicy`가 우선. 앱이 DO를 선언해도 직접 제한
3. 소리/진동 알림은 쓰지 않는다. TTS는 공개됐지만(S6) 주행 무관 알림 금지·Audio Focus 가이드 확인 전까지 끈다
4. 오프라인에서 모든 핵심 기능이 동작해야 한다 (경고 해설은 JSON 지식 베이스, LLM/네트워크 의존 금지)
5. 개인정보 외부 전송 금지. 로컬 데이터는 앱 안에서 삭제 가능해야 한다
6. 하드웨어 직접 접근 금지
7. "진단", "고장 판정" 같은 단정 표현 금지. "안내", "점검 권장"으로 쓴다

## 4. 기능 명세 요약

| 기능 | 핵심 | 수용 기준 |
|---|---|---|
| F0 | `DrivingStateMachine`(주차브레이크·기어·속도, 디바운스 1.5s, 히스테리시스, DRIVING 진입은 즉시 [설계]), `UxPolicy`, `SignalSource` | 상태머신 단위 테스트 |
| F2 | `WarningExplainer`: 경고 → 설명·긴급도(`IMMEDIATE_STOP`/`SERVICE_SOON`/`INFO`)·조치·정비 필요. 주행 중 한 줄+색, 정차 후 상세 | KB 모든 코드의 설명/긴급도 비어 있지 않음, 미지 코드는 "알 수 없는 경고, 점검 권장" 폴백 |
| F4 | "정비소로 안내" → NaviHelper 목적지 설정. POI 검색 없으면 즐겨찾기 정비소 (S5) | 예약은 범위 밖 |
| F3 | 급제동/급가속(속도 변화율 [설계]), 급조향(Steeringwheel). 임계값은 `HabitConfig` | 시나리오 입력에 대해 이벤트가 재현 가능하게 검출 |
| F1 | 주행 중 최대 3개: 활성 경고, 연료/배터리, 안전 점수. 속도/RPM 중복 금지, 앱 영역만 | 항목 3개 초과 금지 |

경고 코드(`WarningCodes`)는 앱 내부 코드다. Safety/Tire API 값과의 매핑은 S2 이후 `core/signal`에서 한다.

## 5. Day-0 Spikes

결과는 `docs/spikes/Sx.md`에 **확인한 사실 / 근거 링크 / 결정**으로 기록한다. 확인 못 한 것은 못 했다고 쓴다.

| ID | 확인할 것 | 막히면 |
|---|---|---|
| S1 | Pleos 에뮬레이터 설치, 빈 앱 실행 | 공식 설치 문서 재확인 |
| S2 | **Safety API 경고 종류/형식**, Safety·Tire capability 확인 | 코어를 F2→F3로 전환 |
| S3 | 속도/기어/주차브레이크 소스 (CarPropertyManager 대조) | 속도 없으면 F3를 조향·페달 중심으로 축소 |
| S4 | 에뮬레이터/VHAL 신호 주입 | `SimulatedSignalSource`로 시연 |
| S5 | NaviHelper 목적지 설정 방식, POI 검색 | 즐겨찾기 정비소 |
| S6 | SDK 저장소/좌표/버전, 권한, TTS/STT/LLM 공개 여부 | 해당 기능 제외 |

## 6. 작업 방식

- 커밋 전: `./gradlew testDebugUnitTest lintDebug` (또는 `scripts/check.sh`). 실패한 상태로 커밋하지 않는다.
- 화면 흐름을 바꿨으면 e2e: `scripts/e2e.sh`
- 브랜치는 기능 단위(`feat/f2-warning` 등), 커밋은 작게
- 레포/README/커밋에 팀명, 이메일, 개인정보, 키/시크릿 금지. 시크릿은 `local.properties`(gitignore)로
- 진행 상황·일정이 바뀌면 README의 "진행 현황"/"일정"을 갱신한다

### 환경 주의 (Windows)

- `JAVA_HOME`은 JDK 17이어야 한다.
- 경로에 한글이 있으면 빌드 산출물은 루트 `build.gradle.kts` 설정으로 `~/.salpim-build/<모듈>`에 생긴다 (테스트 결과·APK·리포트도 거기). 모듈 안 `build/`를 찾지 않는다.
- `android.overridePathCheck=true` 유지, `org.gradle.jvmargs`에 `-Dfile.encoding=UTF-8` 금지 (JVM 테스트 `ClassNotFoundException`).
- adb에 BlueStacks 등의 가짜 기기가 보일 수 있다. e2e는 `scripts/e2e.sh`가 응답하는 기기만 고른다.

### 작업 절차 (`.claude/skills/<이름>/SKILL.md`)

Claude Code는 스킬로 자동 인식한다. 다른 에이전트는 해당 작업을 할 때 그 파일을 직접 읽고 따른다.

| 절차 | 언제 |
|---|---|
| `verify` | 커밋 전 검증 |
| `e2e` | e2e 화면 테스트 실행/추가 |
| `spike` | spike 결과 기록 |
| `add-scenario` | 시뮬레이션 시나리오 추가 |
| `warning-kb` | 경고 지식 베이스 항목 추가/수정 |
