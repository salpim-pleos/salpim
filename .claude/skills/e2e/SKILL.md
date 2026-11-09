---
name: e2e
description: 살핌 e2e 화면 테스트(app/src/androidTest)를 실행하거나 새 테스트를 추가한다. 화면 흐름·주행 상태별 UI를 바꿨을 때, "e2e", "화면 테스트", "에뮬레이터에서 확인" 요청 시 사용.
---

# e2e

## 실행

`scripts/e2e.sh`
- adb에 기기가 연결돼 있으면 `connectedDebugAndroidTest` (Pleos 에뮬레이터를 켜 두면 그 위에서 실행)
- 없으면 Gradle Managed Device `pixel6Api34DebugAndroidTest` (에뮬레이터 부팅 포함 수 분 소요 → background로 실행하고 완료 알림을 기다린다)
- 결과: `app/build/outputs/androidTest-results/**/TEST-*.xml`, HTML은 `app/build/reports/androidTests/`

## 테스트 작성 규칙

- 신호는 `container.simulated`(SimulatedSignalSource)로만 넣는다. 반드시 `rule.runOnUiThread { sim.set { ... } }` (신호원은 Main 디스패처에서 돈다)
- `@Before`에서 `container.resetForTest()` 후 `screen_stopped`를 기다린다
- 상태 전환은 디바운스(1.5초)가 있으니 `rule.waitUntil`로 기다린다. `Thread.sleep` 금지
- 텍스트보다 testTag로 찾는다. 현재 태그:
  - 화면: `screen_driving`, `screen_stopped`, `badge_simulated`
  - 경고: `warning_banner`, `warning_detail`, `warning_item_<CODE>`, `no_warnings`
  - 글랜스: `glance`, `glance_warning`, `glance_energy`, `glance_score`
  - 버튼/기타: `btn_navigate_service`, `nav_message`, `btn_clear_history`, `scenario_<id>`, `coach_report`
- 주행 중 화면에는 입력 요소·상세가 **없음**을 `assertCountEquals(0)`로 검증한다 (안전 규칙 1)
- 자동차 기기에서만/비자동차 기기에서만 의미 있는 테스트는 `assumeTrue`로 건너뛴다 (예: `container.staticUx`)
