# 살핌 (Salpim) — Pleos Connect 상태 인지형 안전 동반 앱

> Claude Code용 프로젝트 컨텍스트 + 설계 문서. 이 파일을 레포 루트에 `CLAUDE.md`로 두면 세션마다 자동으로 읽힌다.
> **규칙: 이 문서와 공식 문서에 없는 Pleos SDK의 클래스/메서드 이름을 추측해서 쓰지 않는다.** 불확실하면 인터페이스 뒤에 숨기고 `TODO(spike-Sx)`를 남긴다.
> 표기: **[확인됨]** = 공식 문서/공고에서 확인, **[미확인]** = Day-0 spike로 검증 필요, **[설계]** = 우리가 정한 결정.

---

## 1. 이름

| 구분 | 추천 | 비고 |
|---|---|---|
| 프로젝트명 | **살핌 (Salpim)** | "살피다"에서 온 이름. 운전 중엔 조용히 지켜보고 필요할 때만 말하는 앱이라는 컨셉과 맞음 |
| 레포명 | **`salpim-pleos-app`** | kebab-case, 대회 이름·팀 정보 미포함 (심사 자료 익명 요건 대응) |
| applicationId | `com.salpim.app` | 변경 가능 |
| 대안 1 | 카독 (CarDoc) / `cardoc-pleos-app` | 직관적이지만 흔한 이름이라 중복 가능성 높음 |
| 대안 2 | 글랜스 (Glance) / `glance-pleos-app` | "한 번 힐끗" 컨셉 강조. 영문 일반명사라 차별성 낮음 |

※ 이름 중복/상표는 확인하지 않았다. 제출 전 검색해 볼 것.

---

## 2. 한 줄 요약과 컨셉

**운전자가 화면을 들여다보지 않아도 되는 안전 앱.** 차량의 주행 상태에 따라 앱이 역할을 바꾼다.

| 상태 | 앱의 역할 | 기능 |
|---|---|---|
| 주행 중 (DRIVING) | 꼭 필요한 정보만 큰 글씨로 요약. 입력 없음 | F1 글랜스 |
| 이상 발생 시 | 경고의 뜻과 긴급도를 한 줄로 알림. 상세 설명은 정차 후 | F2 경고 해설 |
| 정차·주차 후 (STOPPED/PARKED) | 경고 상세, 습관 리포트, 정비소 목적지 설정 | F2 상세, F3 코치, F4 정비 연결 |

핵심 메시지: 심사에서 반려 사유가 되는 "운전자 주의 분산"을 오히려 강점으로 쓴다.

---

## 3. 대회 제약 (변경 시 공식 사이트 우선)

- 대회: 현대자동차그룹 차량용 앱 개발 공모전 "Code your App into Pleos App Market" **[확인됨]**
- 일정 **[확인됨]**: 접수 ~11/15(일) · 1차 결과 11/23 · 2차 PT 12/3~4(오프라인, **동작하는 앱 시연 필수**) · 최종 발표 12/18 · 전시/시상 2027.02
- 1차는 기획서(접수양식 「02 앱 프로젝트 소개/심사 자료」)만으로 지원 가능. **심사 자료는 익명으로 작성** **[확인됨]**
- 평가 기준 **[확인됨]**: 차량특화성 / 창의성 / 기술 활용성(Pleos SDK/API 기여도) / 사업성(타깃, 예상 MAU, Retention, 확장). 가산점: 시연 영상, GitHub(소스+APK)
- 제출 형식 **[확인됨]**: release APK `apk/팀명_앱이름_release.apk`, GitHub는 소스+APK. Private 저장소 허용(심사용 계정을 Collaborator로 초대)
- 개발 환경 **[확인됨]**: Java 또는 Kotlin, Android API 28+, Gradle 8.0+, Android Studio, Pleos Connect 전용 에뮬레이터(설치 문서 기준 시스템 이미지 API 34, RAM 4GB)
- 앱 리뷰 가이드 **[확인됨]**: 운전자 주의 분산·사이버보안 위험이 있으면 반려될 수 있음. 가이드를 지켜도 반려될 수 있음.

---

## 4. 확인된 플랫폼 사실

- Pleos Connect는 Android Automotive OS(AAOS) 기반. Pleos SDK는 AAOS에 같은 기능이 있으면 표준 API를 쓰도록 정렬됨 **[확인됨]**
- Vehicle SDK API 그룹 **[확인됨]**: Brake, CarInfo, Door, DrivingMode, EvBattery, HVAC, Light, Odometer, Safety, Seat, SideMirror, Steeringwheel, Tire, TurnSignal, Window, Wiper
  - `vehicle.getXXX()`로 그룹 객체를 얻고, 시작 시 `vehicle.initialize()`, 종료 시 `vehicle.release()` 호출 (공식 튜토리얼 패턴)
  - Safety = "차량 경고 정보 조회", Brake = "브레이크 페달/주차 브레이크 상태 조회"
- 권한(manifest) **[확인됨]**: `pleos.car.permission.CAR_DRIVE`(DrivingMode, Brake, Odometer), `CAR_ENERGY`(EvBattery), `CAR_SAFETY`(Safety, Tire). 쓰는 도메인 권한만 선언
- 차종 호환성 **[확인됨]**: Vehicle SDK는 차종별로 다름 → `checkXXXCapability` 계열로 반드시 확인. NaviHelper·GleoAI는 완전 호환. **Fused Location, ADAS는 차량 기준 "Not supported" 표기 → 이 앱은 사용하지 않는다.**
- SDK 공개 상태 **[확인됨, 10/22 기준 제3자 정리]**: Vehicle State, NaviHelper는 즉시 사용 가능. Vehicle Control, FusedLocation, TTS, STT는 준비 중. 변동 가능하므로 spike S6에서 재확인
- 하드웨어 직접 접근 금지, 플랫폼 API만 사용. 주행 중 네트워크가 불안정할 수 있으니 오프라인/저대역폭 모드 권장 **[확인됨]**
- 주행 제한 **[확인됨]**: 앱은 `CarUxRestrictionsManager`로 `onUxRestrictionsChanged()`를 받아 `ActiveRestrictions` 플래그별로 기능을 직접 제한. Distraction Optimization을 선언해도 동일
- 공식 튜토리얼 코드가 `setContent { }`를 쓰므로 Jetpack Compose 사용 가능 **[확인됨]**. 튜토리얼은 minSdk 26이라 쓰여 있으나 **대회 요건 28을 따른다**
- SDK 문서가 현재 SDK/에뮬레이터 버전과 다를 수 있다는 경고가 있음 → 문서와 실제 SDK가 다르면 SDK 소스/KDoc이 우선

---

## 5. 기능 명세

우선순위: **P0 기반 → P1 코어(F2+F4) → P2 확장(F3) → P3 확장(F1)**

### F0. 기반 (P0)
- `DrivingState`: `PARKED` / `STOPPED` / `DRIVING` 상태머신
  - 판정 입력: 주차 브레이크, 기어, 속도 (소스는 spike S3에서 확정) **[미확인]**
  - 깜빡임 방지: 상태 전환에 디바운스(기본 1.5초)와 히스테리시스 적용
- `UxRestrictions` 래퍼: `CarUxRestrictions` → 앱 내부 `UxPolicy`(터치 허용, 텍스트 길이 제한, 영상 금지 등)로 변환
- `SignalSource` 인터페이스 + `PleosSignalSource` + `SimulatedSignalSource` (6절)
- 로컬 저장소(Room 또는 DataStore): 경고 이력, 주행 이벤트, 일별 점수. **외부 전송 없음** (개인정보 이슈 최소화)

### F2. 경고 해설 (P1, 코어)
- 입력: Safety API(및 Tire)의 경고 정보 **[미확인: 어떤 경고가 어떤 형식으로 오는지 → S2]**
- `WarningExplainer`: 경고 → `Explanation(쉬운 설명, 긴급도, 권장 조치, 정비 필요 여부)`
  - 긴급도: `IMMEDIATE_STOP`(즉시 안전한 곳에 정차) / `SERVICE_SOON`(가까운 시일 내 점검) / `INFO`
  - **지식 베이스는 앱 내 JSON asset으로 오프라인 동작.** LLM/네트워크에 의존하지 않는다 (주행 중 네트워크 불안정 + LLM의 환각 위험)
  - 지식 베이스 내용은 차량 매뉴얼 기반으로 작성하고, 의학/법률처럼 단정하지 말고 "점검 권장" 톤 유지. 최종 판단은 운전자/정비사
- DRIVING 중: 한 줄 + 아이콘 + 색(긴급도). 터치 불필요. 자동 음성은 TTS SDK 공개 확인 전까지 사용하지 않음
- STOPPED/PARKED: 상세 화면(원인 후보, 해야 할 일, 정비 연결 버튼)
- 수용 기준: 지식 베이스에 있는 모든 경고 코드에 대해 단위 테스트로 설명/긴급도 매핑이 비지 않았음을 검증, 지식 베이스에 없는 코드는 "알 수 없는 경고, 점검 권장"으로 안전하게 폴백

### F4. 정비 연결 (P1, 코어)
- 경고 상세 화면의 "정비소로 안내" → NaviHelper로 목적지 설정 **[확인됨: 목적지 설정 지원]**
- 정비소 선택 방법은 **[미확인]**: NaviHelper에 POI 검색이 있는지, 없으면 사용자가 즐겨찾기로 지정한 정비소 사용 (S5)
- 정비 예약은 대회 범위 밖. 기획서에는 "향후 제휴 확장"으로만 서술

### F3. 습관 코치 (P2, 확장)
- 이벤트: 급제동, 급가속, 급조향
  - Brake API는 페달 **상태**만 제공하므로 감속도는 **속도 샘플의 변화율**로 계산 **[설계]**. 속도 소스는 S3 **[미확인]**
  - 조향각은 Steeringwheel API 사용 가능 **[확인됨: 그룹 존재, 튜토리얼에 조향각 예제]**
  - 임계값은 설정 파일로 분리, 에뮬레이터/시연에서 튜닝
- 주행 중 알림은 최소(이벤트 직후 짧은 시각 표시만, 소리 없음). 정차 후 리포트: 점수, 이벤트 타임라인, 개선 팁 1~2개
- 수용 기준: 시나리오 스크립트 입력에 대해 같은 이벤트가 재현 가능하게 검출됨 (단위 테스트)

### F1. 글랜스 (P3, 확장)
- 주행 중 앱 영역에 표시할 항목은 **최대 3개**: 활성 경고 여부, 연료/배터리 부족 여부, 오늘의 안전 점수
- **좌측 드라이빙뷰/클러스터 영역은 서드파티가 건드릴 수 없다**는 가이드라인 언급이 있으므로 앱 영역에서만 구현 (제3자 커뮤니티 글 인용이라 공식 가이드 재확인 필요) **[미확인]**
- 속도·RPM 같은 기본 정보는 시스템이 이미 보여 주므로 중복하지 않는다. 대신 "지금 신경 써야 할 것"만 표시
- 버튼 크기·간격은 Pleos 디자인 가이드 확인 후 정한다 (진동이 있는 주행 환경 전제)

---

## 6. 아키텍처

```
salpim-pleos-app/
├─ app/                  # Application, Activity, Compose navigation, DI, manifest
├─ core/
│  ├─ model/             # 순수 Kotlin (Android 의존 없음): DrivingState, VehicleSignal, Warning, DrivingEvent, Explanation
│  ├─ signal/            # SignalSource 인터페이스, PleosSignalSource, SimulatedSignalSource  ← Pleos SDK 호출은 여기에만
│  ├─ state/             # DrivingStateMachine
│  ├─ ux/                # CarUxRestrictions 래퍼 → UxPolicy
│  └─ data/              # 로컬 저장소
├─ feature/
│  ├─ warning/           # F2
│  ├─ service/           # F4 (NaviHelper 어댑터는 core/signal 옆의 별도 인터페이스로 격리)
│  ├─ coach/             # F3
│  └─ glance/            # F1
├─ docs/                 # 기획서, 화면 설계, spikes/
├─ apk/                  # release APK (제출용)
└─ CLAUDE.md
```

- 의존 방향: `app → feature/* → core/*`, `core/model`은 아무것도 의존하지 않음
- **Pleos SDK 타입은 `core/signal`(과 NaviHelper 어댑터) 밖으로 새어 나가지 않는다.** 나머지 코드는 `core/model` 타입만 본다. 이렇게 하면 SDK 시그니처가 문서와 달라도 수정 범위가 한 곳으로 한정되고, 에뮬레이터 없이도 단위 테스트가 가능
- UI: Jetpack Compose, 단방향 데이터 흐름(ViewModel + StateFlow). 화면은 `UxPolicy`와 `DrivingState`를 입력으로 받아 렌더링 방식을 결정

```
PleosSignalSource ─┐
                   ├─> SignalSource ─> DrivingStateMachine ─> UxPolicy 결합 ─> feature ViewModels ─> Compose UI
SimulatedSignalSource ┘                        │
                                               └─> WarningExplainer / HabitDetector / Storage
```

### 신호 시뮬레이션 (시연 안정성)
- `SimulatedSignalSource`는 시나리오 스크립트(Kotlin DSL 또는 JSON)로 신호를 재생: `tire_warning`, `hard_brake_sequence`, `parking_after_warning` 등
- 시뮬레이션 신호가 활성이면 **화면에 항상 `SIMULATED` 배지를 표시**한다. 실제 차량/에뮬레이터 신호와 섞어 보이게 만들지 않는다. PT에서도 어떤 부분이 실제 신호이고 어떤 부분이 시뮬레이션인지 사실대로 설명한다
- 에뮬레이터 신호 주입이 가능하면(S4) 그쪽을 우선 사용하고, `SimulatedSignalSource`는 단위 테스트와 백업 시연용으로 유지

---

## 7. 안전/UX 규칙 (어기지 않는다)

1. DRIVING 중: 텍스트 입력 없음, 스크롤/다단계 탐색 없음, 영상·애니메이션 과다 금지, 한 번에 정보 최대 3개
2. `CarUxRestrictions`가 활성이면 `UxPolicy`가 우선. 앱이 DO를 선언해도 직접 제한을 적용
3. 소리/진동 알림은 TTS 공개와 가이드 확인 전까지 쓰지 않는다
4. 오프라인에서 모든 핵심 기능이 동작해야 한다
5. 개인정보 외부 전송 금지. 로컬 저장 데이터는 앱 내에서 삭제 가능해야 함
6. 하드웨어 직접 접근 금지 (플랫폼 API만)
7. 앱 설명에서 "진단", "고장 판정" 같은 단정 표현 금지. "안내", "점검 권장"으로 쓴다

---

## 8. Day-0 Spikes (가장 먼저 한다)

각 spike의 결과는 `docs/spikes/Sx.md`에 **확인한 사실 / 근거 링크 / 결정**으로 기록한다. 확인 못 한 것은 못 했다고 쓴다.

| ID | 확인할 것 | 막히면 |
|---|---|---|
| S1 | Pleos Connect 에뮬레이터 설치, 빈 앱 실행 (API 34 이미지, RAM 4GB, Apple Silicon이면 ARM64 이미지) | 팀원 환경 비교, 공식 설치 문서 재확인 |
| S2 | **Safety API가 반환하는 경고 종류/형식**, Safety·Tire의 capability 확인 방법 | **코어를 F2→F3로 전환**하고 F2는 "향후 확장"으로 기획서 수정 |
| S3 | 속도/기어/주차브레이크 소스. AAOS 표준 프로퍼티(CarPropertyManager)와 "API Compatibility & Availability" 문서의 지원 목록 대조 | 속도가 없으면 F3 급가속/급제동 불가 → 조향·페달 이벤트 중심으로 축소 |
| S4 | 에뮬레이터/Virtual Console 신호 주입 방법. 문서 "ADB Command Reference — Driving restrictions" 및 VHAL 주입 **[본문은 확인하지 못함]** | `SimulatedSignalSource`로 시연 |
| S5 | NaviHelper 목적지 설정 호출 방식, POI 검색 여부 | 사용자 지정 정비소(즐겨찾기) 방식 |
| S6 | SDK 저장소/의존성 좌표/최신 버전, manifest 권한, TTS/STT/LLM 공개 여부 | 해당 기능 제외 |

---

## 9. 일정 (오늘 11/08 기준)

| 기간 | 할 일 |
|---|---|
| 11/08~11/10 | S1~S6 spike, 레포 생성(private), 기획서 초안 |
| 11/11~11/14 | 기획서 완성. 병행: 스캐폴딩 + F0 + `SimulatedSignalSource` (GitHub 가산점용 최소 결과물) |
| **11/15** | **기획서 제출 (마감)** |
| 11/16~11/23 | F2, F4 구현. 결과 발표(11/23) 전에도 계속 진행 |
| 11/24~12/02 | F3, F1, 시연 시나리오 확정, 시연 영상, release APK 빌드 |
| 12/03~12/04 | 2차 PT |

- 1차 결과가 나오기 전에 개발을 멈추지 않는다 (PT까지 약 10일)
- 기능 컷 순서: F1 → F3 → (S2 실패 시) F2. 코어가 흔들리면 범위를 줄이고 시연 완성도를 우선한다

---

## 10. 기획서(접수양식 02) 매핑

| 접수양식 항목 | 이 문서의 근거 |
|---|---|
| 앱 한 줄 소개 | 2절 |
| 기획 배경 | 운전 중 정보 분산, 경고등 의미를 모르는 불안, 주의 분산 없는 안전 앱 필요 (근거 자료는 별도 수집) |
| 주요 기능 및 차량 특화 요소 | 5절, 주행 상태별 역할 전환 |
| 주요 사용 시나리오 | 주행 중 타이어 경고 → 한 줄 안내 → 정차 후 상세 → 정비소 안내 |
| 개발 계획 (Pleos 구현 범위, SDK/API 사용계획) | 4절, 5절, 6절. 사용 SDK: Vehicle(Safety, Tire, Brake, DrivingMode, Steeringwheel), NaviHelper |
| 기대효과·발전 가능성 | 사업성: 정비 예약 제휴, 보험 연계 가능성. **MAU/Retention 수치는 근거 있는 가정으로만 작성** (적용 차종은 그랜저·아반떼·투싼 등으로 확장 중) |

※ 기획서에는 팀명/개인 식별 정보를 넣지 않는다.

---

## 11. Claude Code 작업 지침

- 세션 시작 시 이 문서와 `docs/spikes/`를 먼저 읽는다
- Pleos SDK 호출은 `core/signal`과 NaviHelper 어댑터에만 둔다. 문서에 없는 API는 추측하지 말고 spike 문서에 질문으로 남긴다
- 새 로직은 `core/model` 기반 순수 Kotlin으로 작성해 단위 테스트부터 붙인다
- 커밋 전 실행: `./gradlew testDebugUnitTest lintDebug` (실패한 상태로 커밋하지 않는다)
- 브랜치는 기능 단위(`feat/f2-warning` 등), 커밋은 작게
- 레포에 팀명, 이메일, 개인정보, 키/시크릿을 넣지 않는다. TTS/STT 등 클라이언트 ID/시크릿이 필요하면 `local.properties`로 분리하고 `.gitignore` 처리
- 제출 전에 `apk/` 폴더, 시연 영상 링크, 실행 방법, 테스트 환경을 README에 정리한다 (README에도 익명 유지)

### 첫 세션 추천 지시
1. 프로젝트 스캐폴딩: 위 모듈 구조, Compose, minSdk 28, Gradle 8.x, 빌드 통과
2. `core/model` + `DrivingStateMachine` + 단위 테스트 (디바운스/히스테리시스 포함)
3. `SignalSource` 인터페이스 + `SimulatedSignalSource` + 시나리오 2개 + `SIMULATED` 배지 UI
4. `UxPolicy` 래퍼와 주행 상태별 화면 골격 (DRIVING / STOPPED 화면 분리)

---

## 12. 참고 링크

- 공모전: https://appdevchallenge.auto-hmg.io/
- Pleos Connect SDK 개요: https://document.pleos.ai/en/api-reference/connect-sdk-pleos/
- Vehicle SDK: https://document.pleos.ai/en/api-reference/connect-sdk-pleos/Vehicle/intro
- Vehicle 권한: https://document.pleos.ai/en/api-reference/connect-sdk-pleos/Vehicle/vehicle-permission
- API 호환성/가용성: https://document.pleos.ai/en/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability
- 주행 제한 개발 가이드: https://document.pleos.ai/en/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
- 앱 리뷰 가이드: https://document.pleos.ai/en/docs/pleos-only/distribute/app-review-guide/
- Vehicle SDK 튜토리얼: https://document.pleos.ai/en/docs/pleos-only/tutorial/vehicle-sdk
- 샘플: https://github.com/Pleos-Playground
