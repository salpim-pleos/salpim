# 살핌 (Salpim)

> 운전자가 화면을 들여다보지 않아도 되는 Pleos Connect 안전 동반 앱

차량의 주행 상태에 따라 앱이 역할을 바꾼다. 주행 중에는 조용히 지켜보고, 꼭 필요할 때만 한 줄로 알리고, 정차한 뒤에 자세히 설명한다.

| 상태 | 앱의 역할 | 기능 |
|---|---|---|
| 주행 중 (DRIVING) | 꼭 필요한 정보만 큰 글씨로 요약. 입력 없음 | F1 글랜스 |
| 이상 발생 시 | 경고의 뜻과 긴급도를 한 줄로 알림. 상세 설명은 정차 후 | F2 경고 해설 |
| 정차·주차 후 (STOPPED/PARKED) | 경고 상세, 습관 리포트, 정비소 목적지 설정 | F2 상세, F3 코치, F4 정비 연결 |

핵심 메시지: 심사 반려 사유인 "운전자 주의 분산"을 오히려 강점으로 쓴다.

---

## 목차

- [기능](#기능)
- [진행 현황](#진행-현황)
- [레포 구조](#레포-구조)
- [개발 환경과 빌드](#개발-환경과-빌드)
- [테스트](#테스트)
- [신호 시뮬레이션](#신호-시뮬레이션)
- [일정](#일정)
- [대회 정보](#대회-정보)
- [기획서(접수양식 02) 매핑](#기획서접수양식-02-매핑)
- [제출 체크리스트](#제출-체크리스트)
- [참고 링크](#참고-링크)

---

## 기능

우선순위: **P0 기반 → P1 코어(F2+F4) → P2 확장(F3) → P3 확장(F1)**. 기능 컷 순서는 F1 → F3 → (S2 실패 시) F2.

| ID | 기능 | 요약 |
|---|---|---|
| F0 | 기반 | 주행 상태 판정(PARKED/STOPPED/DRIVING, 디바운스·히스테리시스), 주행 제한 정책(UxPolicy), 신호원 추상화, 로컬 저장 |
| F2 | 경고 해설 (코어) | 경고 → 쉬운 설명·긴급도·권장 조치. 앱 내 지식 베이스로 오프라인 동작. 주행 중 한 줄, 정차 후 상세 |
| F4 | 정비 연결 (코어) | 경고 상세에서 "정비소로 안내" → NaviHelper 목적지 설정 |
| F3 | 습관 코치 | 급제동·급가속·급조향 검출, 정차 후 점수와 개선 팁 |
| F1 | 글랜스 | 주행 중 최대 3개 항목: 활성 경고, 연료/배터리 부족, 오늘의 안전 점수 |

상세 명세와 수용 기준은 [AGENTS.md](AGENTS.md)를 본다.

## 진행 현황

| 항목 | 상태 |
|---|---|
| 스캐폴딩 (멀티모듈, Compose, minSdk 28) | 완료 |
| F0 상태머신 / UxPolicy / SimulatedSignalSource + 시나리오 3개 | 완료 (단위 테스트) |
| 테스트 환경 | 단위 38개 + e2e 5개 통과 (2026-11-09) |
| F2 지식 베이스 초안 8종 + 해설기 + 주행/정차 UI | 초안. 문구는 차량 매뉴얼로 검증 필요, 실제 경고 매핑은 S2 이후 |
| F4 블루핸즈 안내·예약 | 블루핸즈 목록은 **mock**(`feature/service/src/main/assets/bluehands_mock.json`, 화면에 "예시 데이터" 표시). TODO: 카카오 로컬 API 검색, NaviHelper 길 안내(S5), 예약 QR, 제휴 예약 API |
| 디자인 기준 | [docs/DESIGN.md](docs/DESIGN.md) (Pleos 디자인 가이드 + 현대 웹 액션 스타일). Compose 화면은 아직 이전 스타일 |
| F3 습관 검출 + 리포트 | 초안. 임계값 튜닝 필요 |
| F5 주유 예측 | 판단 로직 완료 (`feature/fuel`: 연비 분포 75%/90% 분위 + 결정 트리, 테스트 15개). 앱 연결·주유소 mock·실제 Fuel/Odometer API는 다음 |
| 클린 코드 | AGENTS.md 7절 + detekt/ktlint (`scripts/check.sh`에서 강제) |
| F1 글랜스 | 초안 |
| Pleos SDK 연동 (`PleosSignalSource`) | 미착수 — S2/S3/S6 대기 |
| Day-0 spike S1~S6 | 문서 조사 완료 (2026-11-09), 에뮬레이터 확인 대기 → [docs/spikes](docs/spikes). **S2/S3 결과로 F2·F3 범위 조정 필요** |
| 기획서 초안 | [docs/proposal.md](docs/proposal.md) |

## 레포 구조

```
salpim/
├─ app/                  # Application, Activity, 화면, 엔진(신호 파이프라인), 수동 DI
│  ├─ src/test/          # 파이프라인 통합 테스트 (JVM, 가상 시간)
│  └─ src/androidTest/   # e2e 화면 테스트 (Compose UI test)
├─ core/
│  ├─ model/             # 순수 Kotlin 모델 (Android 의존 없음)
│  ├─ state/             # DrivingStateMachine
│  ├─ signal/            # SignalSource, SimulatedSignalSource, 시나리오, Pleos·NaviHelper 어댑터
│  ├─ ux/                # CarUxRestrictions 래퍼 → UxRestrictionFlags
│  └─ data/              # 로컬 기록 (현재 메모리, 추후 Room/DataStore)
├─ feature/
│  ├─ warning/           # F2 경고 해설 (지식 베이스: src/main/assets/warnings_kb.json)
│  ├─ service/           # F4 정비소 안내
│  ├─ coach/             # F3 습관 코치
│  └─ glance/            # F1 글랜스
├─ docs/spikes/          # Day-0 spike 기록 (S1~S6)
├─ scripts/              # check.sh, e2e.sh
├─ apk/                  # 제출용 release APK
├─ .claude/skills/       # Claude Code 작업 스킬
├─ AGENTS.md             # 개발 규칙 (모든 코딩 에이전트 공통)
└─ CLAUDE.md             # Claude Code용 진입점 (AGENTS.md를 불러옴)
```

의존 방향: `app → feature/* → core/*`. `core/model`은 아무것도 의존하지 않는다. Pleos SDK 타입은 `core/signal` 밖으로 나가지 않는다.

```
PleosSignalSource ─┐
                   ├─> SignalSource ─> DrivingStateMachine ─> UxPolicy ─> MainViewModel ─> Compose UI
SimulatedSignalSource ┘                     │
                                            └─> WarningExplainer / HabitDetector / History
```

## 개발 환경과 빌드

| 항목 | 버전 |
|---|---|
| JDK | 17 (Android Studio 내장 JBR 또는 Temurin 17) |
| Gradle | 8.9 (wrapper 포함) |
| AGP / Kotlin | 8.5.2 / 2.0.21 |
| compileSdk / targetSdk / minSdk | 34 / 34 / 28 |
| UI | Jetpack Compose (Material 3) |

```bash
./gradlew assembleDebug                    # 디버그 APK → app/build/outputs/apk/debug/ (한글 경로면 ~/.salpim-build/app/outputs/apk/debug/)
./gradlew testDebugUnitTest lintDebug      # 커밋 전 필수 (scripts/check.sh)
./gradlew assembleRelease                  # 제출용 (서명 설정 TODO)
```

Windows 주의:
- `JAVA_HOME`이 JDK 17을 가리켜야 한다 (Java 8로는 AGP 8이 동작하지 않음).
- 프로젝트 경로에 한글 등 비ASCII 문자가 있으면 **빌드 산출물이 자동으로 `~/.salpim-build/<모듈>`에 생성**된다 (adb가 한글 경로의 APK를 설치하지 못하기 때문). 소스는 그대로 두면 된다. 위치를 바꾸려면 `-Psalpim.buildRoot=<경로>`.
- 같은 이유로 `gradle.properties`에 `android.overridePathCheck=true`가 있고, `org.gradle.jvmargs`에 `-Dfile.encoding=UTF-8`을 넣지 않는다 (넣으면 JVM 테스트가 `ClassNotFoundException`으로 실패).
- `local.properties`(sdk.dir)는 커밋하지 않는다.

## 테스트

| 종류 | 위치 | 실행 |
|---|---|---|
| 단위 테스트 | 각 모듈 `src/test` | `./gradlew testDebugUnitTest` |
| 파이프라인 통합 | `app/src/test` (가상 시간으로 시나리오 재생) | 위와 같음 |
| e2e 화면 테스트 | `app/src/androidTest` | `scripts/e2e.sh` |

e2e는 `SimulatedSignalSource`로 신호를 넣고 실제 Activity 화면을 검증한다.
- 응답하는 기기/에뮬레이터가 있으면 `connectedDebugAndroidTest`, 없으면 Gradle Managed Device `pixel6Api34`(API 34, google 이미지)를 띄워 실행한다. 강제 선택은 `E2E_TARGET=connected|managed scripts/e2e.sh`.
- BlueStacks 등 다른 앱이 adb 포트를 열어 두면 가짜 기기(`emulator-5554`)가 보일 수 있다. 스크립트는 `adb shell`에 응답하지 않는 기기를 건너뛴다.
- 최근 결과 (2026-11-09, pixel6Api34): 5/5 통과.
- Pleos Connect 에뮬레이터를 켜 두면 그 위에서 실행된다 (자동차 기기에서는 실제 CarUxRestrictions를 쓰므로 일부 테스트는 건너뜀).
- 검증 항목: SIMULATED 배지 상시 표시, 주행 중 한 줄 경고·상세/버튼 없음·정보 3개 이하, 주차 후 상세와 정비소 안내, 시나리오 버튼, 시스템 주행 제한 시 주행 화면 강제.

## 신호 시뮬레이션

Pleos SDK 연동 전이거나 시연 백업이 필요할 때 `SimulatedSignalSource`가 시나리오를 재생한다.

| id | 내용 |
|---|---|
| `tire_warning` | 출발 → 60km/h 주행 중 타이어 공기압 경고 |
| `hard_brake_sequence` | 급가속 1회 + 급제동 1회 |
| `parking_after_warning` | 경고 상태로 주행 → 정차 → 주차 |

- 시뮬레이션이 활성이면 화면 우상단에 항상 **SIMULATED** 배지가 뜬다. PT에서도 실제 신호와 시뮬레이션을 구분해 설명한다.
- 정차 화면 오른쪽 아래의 시나리오 버튼으로 재생한다. 정비소는 시연용 "예시 정비소"가 미리 지정되어 있다.
- 에뮬레이터 신호 주입(S4)이 가능해지면 그쪽을 우선 쓰고, 시뮬레이션은 테스트·백업용으로 유지한다.

## 일정

2026-11-08 기준.

| 기간 | 할 일 |
|---|---|
| 11/08~11/10 | S1~S6 spike, 레포 생성(private), 기획서 초안 |
| 11/11~11/14 | 기획서 완성. 병행: 스캐폴딩 + F0 + SimulatedSignalSource (GitHub 가산점용 최소 결과물) |
| **11/15** | **기획서 제출 마감** |
| 11/16~11/23 | F2, F4 구현. 결과 발표(11/23) 전에도 계속 진행 |
| 11/24~12/02 | F3, F1, 시연 시나리오 확정, 시연 영상, release APK 빌드 |
| 12/03~12/04 | 2차 PT |

- 1차 결과가 나오기 전에도 개발을 멈추지 않는다 (PT까지 약 10일).
- 코어가 흔들리면 범위를 줄이고 시연 완성도를 우선한다.

## 대회 정보

현대자동차그룹 차량용 앱 개발 공모전 "Code your App into Pleos App Market" (변경 시 공식 사이트 우선)

- 일정: 접수 ~11/15(일) · 1차 결과 11/23 · 2차 PT 12/3~4(오프라인, **동작하는 앱 시연 필수**) · 최종 발표 12/18 · 전시/시상 2027.02
- 1차는 기획서(접수양식 「02 앱 프로젝트 소개/심사 자료」)만으로 지원 가능. **심사 자료는 익명으로 작성**
- 평가 기준: 차량특화성 / 창의성 / 기술 활용성(Pleos SDK/API 기여도) / 사업성(타깃, 예상 MAU, Retention, 확장). 가산점: 시연 영상, GitHub(소스+APK)
- 제출 형식: release APK `apk/팀명_앱이름_release.apk`, GitHub는 소스+APK. Private 저장소 허용(심사용 계정을 Collaborator로 초대)
- 개발 환경: Java 또는 Kotlin, Android API 28+, Gradle 8.0+, Pleos Connect 전용 에뮬레이터(시스템 이미지 API 34, RAM 4GB)
- 앱 리뷰 가이드: 운전자 주의 분산·사이버보안 위험이 있으면 반려될 수 있음

### 이름

| 구분 | 값 |
|---|---|
| 프로젝트명 | 살핌 (Salpim) — "살피다". 운전 중엔 조용히 지켜보고 필요할 때만 말하는 앱 |
| 레포 | `salpim` |
| applicationId | `com.salpim.app` |

이름 중복/상표는 확인하지 않았다. 제출 전 검색해 볼 것.

## 기획서(접수양식 02) 매핑

| 접수양식 항목 | 근거 |
|---|---|
| 앱 한 줄 소개 | 맨 위 소개 |
| 기획 배경 | 운전 중 정보 분산, 경고등 의미를 모르는 불안, 주의 분산 없는 안전 앱 필요 (근거 자료는 별도 수집) |
| 주요 기능 및 차량 특화 요소 | [기능](#기능), 주행 상태별 역할 전환 |
| 주요 사용 시나리오 | 주행 중 타이어 경고 → 한 줄 안내 → 정차 후 상세 → 정비소 안내 |
| 개발 계획 (Pleos 구현 범위, SDK/API 사용계획) | 사용 SDK: Vehicle(Safety, Tire, Brake, DrivingMode, Steeringwheel), NaviHelper. 구조는 [레포 구조](#레포-구조) |
| 기대효과·발전 가능성 | 정비 예약 제휴, 보험 연계 가능성. **MAU/Retention 수치는 근거 있는 가정으로만** (적용 차종은 그랜저·아반떼·투싼 등으로 확장 중) |

기획서와 이 README에는 팀명/개인 식별 정보를 넣지 않는다.

## 제출 체크리스트

- [ ] 기획서 제출 (11/15)
- [ ] release APK 서명 빌드 → `apk/팀명_앱이름_release.apk`
- [ ] 시연 영상 링크
- [ ] 테스트 환경 기록 (에뮬레이터 버전, SDK 버전)
- [ ] 심사용 계정 Collaborator 초대
- [ ] README/커밋/코드에 팀명·이메일·개인정보·키 없음 확인

## 참고 링크

- 공모전: https://appdevchallenge.auto-hmg.io/
- Pleos Connect SDK 개요: https://document.pleos.ai/en/api-reference/connect-sdk-pleos/
- Vehicle SDK: https://document.pleos.ai/en/api-reference/connect-sdk-pleos/Vehicle/intro
- Vehicle 권한: https://document.pleos.ai/en/api-reference/connect-sdk-pleos/Vehicle/vehicle-permission
- API 호환성/가용성: https://document.pleos.ai/en/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability
- 주행 제한 개발 가이드: https://document.pleos.ai/en/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
- 앱 리뷰 가이드: https://document.pleos.ai/en/docs/pleos-only/distribute/app-review-guide/
- Vehicle SDK 튜토리얼: https://document.pleos.ai/en/docs/pleos-only/tutorial/vehicle-sdk
- 샘플: https://github.com/Pleos-Playground
