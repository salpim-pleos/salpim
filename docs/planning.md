# 진행 계획과 대회 정보 (내부용)

README에서 옮긴 내용. 공개 저장소이므로 개인 식별 정보는 넣지 않는다.

## 대회

현대자동차그룹 차량용 앱 개발 공모전 "Code your App into Pleos App Market" (변경 시 공식 사이트 우선 — https://appdevchallenge.auto-hmg.io/)

- 일정: 접수 ~11/15(일) · 1차 결과 11/23 · 2차 PT 12/3~4(오프라인, 동작하는 앱 시연 필수) · 최종 발표 12/18 · 전시/시상 2027.02
- 1차는 접수양식(= 기획서)으로 심사. 심사 자료와 공유 링크는 익명
- 평가: 차량특화성 / 창의성 / 기술 활용성(Pleos SDK/API) / 사업성. 가산점: 시연 영상, GitHub, APK
- 제출: release APK 파일명 `팀명_앱이름_release.apk`(개인은 성명) — 실명이 들어가므로 공개 저장소에는 올리지 않고 이메일로 제출

## 일정

| 기간 | 할 일 |
|---|---|
| ~11/15 | 기획서(접수양식) 제출 — 2026-11-10 제출 완료 (APK는 메일 첨부가 막혀 저장소 링크로) |
| 11/16~11/23 | Pleos 에뮬레이터에서 SDK 연동 검증, 점검 시점 예상 |
| 11/24~12/02 | 음성 SDK 검토, 시연 시나리오, 시연 영상, release APK |
| 12/03~12/04 | 2차 PT |

## 남은 일

1. Pleos 에뮬레이터 설치(S1) → `PleosSignalSource`·NaviHelper 실동작 확인 (주행거리 단위, poiId 빈 값, 목적지 남은 거리)
2. 점검 시점 예상 기능 (기획서·화면 시안에 있음, 코드 없음)
3. NaviHelper 검색 화면 버튼 (예약 QR은 2026-11-10 완료)
4. 경고 설명 문구를 차량 매뉴얼로 검증
5. 급조향 임계값을 앞바퀴 조향각 기준으로 재조정
6. 차속: AAOS CarPropertyManager(PERF_VEHICLE_SPEED) 사용 가능 여부
7. 시연 영상·서명 APK는 `release/`에 커밋됨 (GitHub Release는 작성자 계정이 드러나 쓰지 않음). 화면이 바뀌면 다시 녹화

## 참고 링크

- Pleos Connect SDK: https://document.pleos.ai/en/api-reference/connect-sdk-pleos/
- API 호환성/가용성: https://document.pleos.ai/en/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability
- 주행 제한 개발 가이드: https://document.pleos.ai/en/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
- 앱 리뷰 가이드: https://document.pleos.ai/en/docs/pleos-only/distribute/app-review-guide/
- 샘플: https://github.com/Pleos-Playground
