---
name: clean-code
description: 살핌 코드를 작성하거나 리뷰할 때 AGENTS.md 7절 클린 코드 규칙을 적용·점검한다. 새 기능 구현 전후, "클린 코드", "리팩터링", "리뷰" 요청 시 사용.
---

# clean-code

## 작성 전

1. 판단 로직(계산·결정)을 어디에 둘지 먼저 정한다: 순수 Kotlin 도메인 함수 (`core/*` 또는 feature의 비UI 파일). Android/Compose/SDK 의존 금지
2. 입력·출력 타입을 data class/sealed로 먼저 정의한다. 단위는 이름에 (`rangeKm`, `elapsedMs`)
3. 임계값·계수는 `XxxConfig` data class 기본값으로. 코드 안에 숫자를 쓰지 않는다
4. 경계값 테스트부터 쓴다 (임계 바로 아래/위, null 입력, 빈 목록)

## 작성 후

1. `./gradlew detekt --auto-correct` 로 포맷 자동 수정
2. `scripts/check.sh` (detekt + 단위 테스트 + lint) 통과 확인
3. 아래 리뷰 체크리스트로 바뀐 파일을 훑는다

## 리뷰 체크리스트 (AGENTS.md 7.2)

- [ ] 이름만 보고 무엇을 하는지 알 수 있는가, 단위가 이름에 있는가
- [ ] 함수가 계산과 부수 효과를 섞고 있지 않은가
- [ ] Boolean 플래그 여러 개 대신 sealed/enum으로 상태를 표현했는가
- [ ] 예상 가능한 실패를 결과 타입으로 다루는가
- [ ] SDK/Android 타입이 도메인 계층으로 새지 않았는가
- [ ] ViewModel에 계산 로직이 들어가지 않았는가
- [ ] 화면 수치가 `core/designsystem` 토큰을 쓰는가
- [ ] 주석이 "왜"를 말하는가, TODO가 `TODO(태그):` 형식인가
- [ ] 새 판단 로직에 경계값 테스트가 있는가
- [ ] 죽은 코드·주석 처리한 코드가 없는가

detekt를 끄는 `@Suppress`는 사유 주석과 함께, 최소 범위(한 줄/한 함수)로만 쓴다.
