---
name: warning-kb
description: F2 경고 지식 베이스(feature/warning/src/main/assets/warnings_kb.json) 항목을 추가하거나 문구를 고친다. 새 경고 코드 지원, 경고 설명 문구 검토 시 사용.
---

# warning-kb

1. 코드: `core/model/.../Warning.kt`의 `WarningCodes`에 상수를 추가한다 (앱 내부 코드, UPPER_SNAKE). SDK 값은 여기 쓰지 않는다 — 매핑은 S2 이후 `core/signal`에서.
2. `warnings_kb.json`의 `entries`에 항목 추가. 필드 모두 필수:
   `code, title, oneLine, detail, causes[], actions[], urgency(IMMEDIATE_STOP|SERVICE_SOON|INFO), needsService`
3. 문구 규칙 (안전/UX 규칙 7):
   - `oneLine`은 40자 이하, 주행 중 한 번에 읽히게
   - "고장입니다", "진단 결과", "고장 판정", "확실히" 같은 단정 표현 금지. "~일 수 있어요", "점검을 권장해요"
   - 원인은 "가능성"으로 쓴다. 최종 판단은 운전자/정비사
   - 내용은 차량 매뉴얼 기반. 근거를 확인 못 한 문구는 사용자에게 검증 필요하다고 알린다
4. 긴급도: 계속 주행이 위험할 수 있으면 `IMMEDIATE_STOP`, 점검이 필요하면 `SERVICE_SOON`, 단순 안내는 `INFO`.
5. `feature/warning/src/test/.../WarningExplainerTest.kt`의 `allAppWarningCodesAreCovered` 목록을 갱신하고 `verify` 스킬로 검증.
