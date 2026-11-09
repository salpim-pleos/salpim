---
name: add-scenario
description: SimulatedSignalSource용 시연/테스트 시나리오를 추가한다. 시연 흐름이나 새 경고·주행 이벤트를 재현해야 할 때 사용.
---

# add-scenario

1. `core/signal/src/main/kotlin/com/salpim/core/signal/sim/Scenarios.kt`에 추가하고 `all` 목록에 넣는다.
   - id는 snake_case. 정차 화면 버튼의 testTag가 `scenario_<id>`가 된다
   - DSL: `set { copy(...) }`(즉시 변경), `hold(ms)`, `ramp(toKmh, ms)`(선형 속도 변화)
   - 출발은 `departure(kmh)`(6 km/h/s, 급가속 아님)를 쓴다
2. 습관 코치 임계값(`HabitConfig`)을 의식한다: 급제동 ≥ 10 km/h/s, 급가속 ≥ 9 km/h/s, 급조향 ≥ 90° & ≥ 40 km/h, 같은 종류 3초 쿨다운. 의도하지 않은 이벤트가 나오지 않게 ramp 기울기를 계산한다.
3. 테스트:
   - `core/signal/src/test/.../ScenarioTest.kt`: 프레임 결과(최종 상태, 경고 시점) 검증
   - 이벤트가 목적이면 `feature/coach/src/test/.../HabitDetectorTest.kt`에 검출 개수 검증
   - 파이프라인 결과가 중요하면 `app/src/test/.../SalpimEngineTest.kt` (가상 시간 `advanceTimeBy`)
4. README "실행" 절의 시나리오 목록에 추가한다.
5. `verify` 스킬로 검증.
