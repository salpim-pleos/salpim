# 살핌 (Salpim)

> 운전 중엔 말로 묻고 듣고, 차에 이상이 생기거나 점검·주유할 때가 다가오면 정차 후 블루핸즈 안내와 예약까지 이어 주는 Pleos Connect 차량 관리 동반 앱

차를 잘 몰라도 됩니다. 경고가 뜨면 쉬운 말 한 줄로 알려 주고, 운전 중에는 음성으로 묻고 답하며, 정차하면 자세한 설명과 다음 할 일(블루핸즈 안내·예약, 주유)까지 이어 줍니다. 기획 배경과 근거는 [docs/proposal.md](docs/proposal.md)에 있습니다.

**데모**: 서명된 APK와 시연 영상(시뮬레이션 신호)은 [Releases v0.1.0-demo](https://github.com/salpim-pleos/salpim/releases/tag/v0.1.0-demo)에 있습니다.

## 주요 기능

| 기능 | 내용 |
|---|---|
| 주행 상태별 화면 | 주차·정차·주행을 판정해 화면 역할을 바꿉니다. 주행 중에는 입력·스크롤 없이 정보 3개만, 시스템 주행 제한(CarUxRestrictions)을 우선 적용 |
| 차량 이상 해설 | Vehicle SDK Safety 신호(엔진오일·브레이크액·변속기오일 레벨, 엔진 이상, 워셔액, 안전벨트, 에어백)를 쉬운 말과 긴급도로 설명. 위에서 본 차 그림에서 문제 부위만 한 번 밝아지며 켜짐. 앱 내 지식 베이스로 오프라인 동작 |
| 주유 예측 | 이 차의 "연료 1%당 km" 기록 분포로 남은 거리를 두 단계(지난 주행의 75%·90%를 덮는 거리)로 예측하고, 결정 트리로 충분 / 가는 길에 주유 / 지금 주유를 판단 |
| 블루핸즈 안내·예약 | 가까운 블루핸즈를 골라 NaviHelper로 길 안내, 휴대폰 QR로 공식 정비 예약 페이지 연결 |
| 음성 제어 | "차 상태 어때?", "계속 달려도 돼?", "가까운 블루핸즈 찾아 줘", "첫 번째로 가 줘", "얼마나 더 갈 수 있어?"처럼 말로 조작하고, 답은 소리와 화면 글자로 함께 |
| 운전 습관 리포트 | 급가속·급제동·급조향을 검출해 정차 후 점수와 개선 팁 |

## 실행

요구 사항: JDK 17, Android SDK 34. Pleos Connect 에뮬레이터(API 34)에서 실제 차량 신호로, 일반 에뮬레이터에서는 시뮬레이션 신호로 동작합니다.

```bash
./gradlew assembleDebug                              # 디버그 APK
./gradlew assembleDebug -Psalpim.signalSource=sim    # Pleos 에뮬레이터에서도 시연 시나리오(시뮬레이션) 사용
```

| `salpim.signalSource` | 신호원 |
|---|---|
| `auto` (기본) | 자동차 기기면 Pleos Vehicle SDK, 아니면 시뮬레이션 |
| `pleos` | 항상 Pleos Vehicle SDK |
| `sim` | 항상 시뮬레이션. 화면에 "시뮬레이션 신호"가 항상 표시되고, 정차 화면에서 시나리오를 재생할 수 있음 |

시뮬레이션 시나리오: `engine_oil_warning`(주행 중 엔진오일 부족), `hard_brake_sequence`(급가속·급제동), `parking_after_warning`(경고 후 정차·주차), `low_fuel_trip`(연료 18%로 220km 장거리 출발).

## 사용한 Pleos SDK / API

| SDK / API | 용도 | 권한 |
|---|---|---|
| Vehicle 2.3.2 — Safety | 오일 레벨, 엔진 이상, 워셔액, 안전벨트, 에어백 전개 | `CAR_SAFETY` |
| Vehicle — Gear, Odometer | 기어(주행 상태 판정), 누적 주행거리(주유 예측) | `CAR_DRIVE` |
| Vehicle — Fuel | 연료 잔량 | `CAR_ENERGY` |
| NaviHelper 3.0.5 | 블루핸즈·주유소 길 안내, 내비 장소 검색 화면 | `NAVI_ROUTE_SEARCH`, `NAVI_CUSTOM_MAP` |
| AAOS CarUxRestrictionsManager | 주행 제한 반영 | — |

SDK 클래스·메서드·권한은 배포된 AAR에서 직접 확인했습니다 ([docs/spikes](docs/spikes)). 서드파티에 허용되지 않는 API(계기판 경고등, 타이어 공기압)는 쓰지 않습니다.

## 구조

```
app/                화면(Compose), 신호 파이프라인, 음성 대화
core/model          순수 Kotlin 모델
core/state          주행 상태머신 (디바운스·히스테리시스)
core/signal         신호원 — Pleos SDK 어댑터, 시뮬레이션, NaviHelper 어댑터 (SDK 타입은 이 모듈 밖으로 나가지 않음)
core/ux             주행 제한(CarUxRestrictions) 래퍼
core/designsystem   디자인 토큰, Asta Sans, 공용 컴포넌트, 차 그림
core/data           운전 기록
feature/warning     차량 이상 해설 (지식 베이스: src/main/assets/warnings_kb.json)
feature/fuel        주유 예측 (분위수 + 결정 트리)
feature/service     블루핸즈·주유소 안내
feature/coach       운전 습관 검출
feature/voice       한국어 음성 명령 해석, 음성 입출력
```

## 테스트

```bash
scripts/check.sh    # 정적 분석(detekt·ktlint) + 단위 테스트 + Android lint
scripts/e2e.sh      # 화면 흐름 테스트 (연결된 에뮬레이터 또는 Gradle Managed Device)
```

단위 테스트 123개와 화면 흐름 테스트 6개가 있습니다 (주행 중 입력 요소 없음, 음성 명령으로 블루핸즈 찾기와 길 안내, 시스템 주행 제한 반영 등).

## 현재 한계

- 블루핸즈·주유소 목록은 **예시 데이터**이며 화면에 그렇게 표시됩니다. 장소 검색을 인터페이스로 분리해 두었으므로, 제휴 시 현대 블루핸즈 데이터·예약 API로 교체하는 것을 전제로 합니다 (외부 지도 API·위치 전송 없음)
- Vehicle SDK에 차속 API가 없어 실제 차량 신호에서는 급가속·급제동 검출이 동작하지 않습니다 (시뮬레이션에서는 동작)
- 음성 입출력은 Android 기본 엔진으로 구현했고 Pleos STT/TTS SDK로 교체를 검토 중입니다

## 라이선스 고지

- 서체: [Asta Sans](https://github.com/42dot/Asta-Sans) — SIL Open Font License 1.1 (`core/designsystem/FONT-LICENSE-OFL.txt`)
