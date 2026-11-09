---
name: Salpim
base: Pleos Connect Design Guide (foundations) + Hyundai KR web action style
colors:
  bg: '#131417'            # Pleos basic_00 (background_secondary, dark)
  surface: '#1D1E21'       # Pleos basic_50 (background_primary, dark)
  surface-raised: '#313236' # Pleos basic_100 (popup)
  line: '#313236'
  text: '#F7F8FA'          # Pleos basic_800
  text-secondary: '#A4A8AE' # Pleos basic_400
  text-tertiary: '#686A72' # Pleos basic_300 — 큰 글자(24+)에만
  action: '#002C5F'        # Hyundai navy — 주 액션 버튼 배경 (흰 글자)
  action-on: '#FFFFFF'
  selected: '#007FA8'      # Hyundai teal — 선택 표시(면/선)에만, 글자색으로 쓰지 않음
  urgent: '#FF4339'        # Pleos regulation red — IMMEDIATE_STOP
  caution: '#FFC224'       # Pleos regulation yellow — SERVICE_SOON
  notice: '#0082FF'        # Pleos regulation blue — INFO
  ok: '#55F165'            # Pleos regulation green — 이상 없음
  simulated: '#FF8A00'     # Pleos regulation orange — SIMULATED 표시 (테두리/글자)
typography:
  family: Asta Sans (42dot, OFL-1.1, Google Fonts) — Pleos 시스템 서체. 대체: Pretendard
  headline: 60/80, 56/72, 48/64 (ExtraBold, Bold)
  title: 40/52, 36/44, 32/40
  body: 30/38, 28/36, 26/34 (Strong, Normal)
  label: 24/28, 20/24
  min: 24 (주행 중), 20 (정차 화면 보조 라벨만)
rounded:
  action: 0px              # Hyundai 웹의 직사각형 액션
  container: 8px
---

# 살핌 디자인 기준

## 출처

| 출처 | 가져온 것 | 라이선스 |
|---|---|---|
| [Pleos Design Guide — Colors](https://document.pleos.ai/en/docs/connect/guide/docs-design/foundations/colors), [Typography](https://document.pleos.ai/en/docs/connect/guide/docs-design/foundations/typography), Vehicle Notification, Window | 색 토큰(basic, regulation), 타이포 스케일, 경고 구조, 1:1 분할 창 정책 | 공식 가이드 |
| [Asta Sans](https://github.com/42dot/Asta-Sans) | 서체 | OFL-1.1 |
| [oh-my-design `hyundai/DESIGN.md`](https://github.com/kwakseongjae/oh-my-design) (현대 KR 웹 분석) | 네이비 `#002C5F` 액션, 틸 `#007FA8` 선택 표시, 0px 직사각형 액션, 흑백 위주 절제 | MIT |
| [Android for Cars — typography/sizing](https://developers.google.com/cars/design/automotive-os/design-system/typography) | 최소 글자 24dp, 4dp 그리드, 터치 타깃 76dp | 공식 가이드 |

Hyundai Sans는 공개 라이선스가 없어 쓰지 않는다. 현대/블루핸즈 로고·상표 이미지는 넣지 않는다 (명칭 텍스트만).

## 원칙

1. **색은 상태가 있을 때만.** 평상시 화면은 흑백·회색. 경고/점검이 있을 때만 regulation 색이 나타난다. 색만으로 구분하지 않고 아이콘+텍스트를 같이 쓴다.
2. **카드 대신 여백과 타이포.** 둥근 회색 카드 나열 금지. 위계는 크기·굵기·여백·얇은 구분선(`line`)으로.
3. **주행 중은 계기판처럼.** 큰 값 하나 + 짧은 라벨. 최대 3항목. 속도·텔테일은 시스템 드라이빙뷰가 보여 주므로 중복하지 않는다.
4. **경고는 Vehicle Notification 구조.** 아이콘(48dp 그리드) + 제목(Title 40) + 설명(Body 28). 실제 계기판 텔테일과 똑같이 그리지 않는다 (혼동 방지).
5. **액션은 네이비 직사각형.** 주 액션 1개는 `action` 배경 + 흰 글자, 0px 모서리, 높이 76 이상. 보조 액션은 테두리만.
6. **움직임 최소.** 주행 중 루프·펄스·깜빡임 금지. 눌림은 Pleos 기준대로 95% 축소 + 배경 변화만.
7. **1:1 분할 창 대응.** 가로 2분할(넓은 창)과 세로 쌓기(좁은 창) 모두 동작.

## 상태별 화면

| 상태 | 구성 |
|---|---|
| 주행 · 평상시 | 3열 글랜스: 차량 상태 / 다음 점검 / 오늘 안전 점수. 열 사이 세로 구분선. 값은 Headline 56 |
| 주행 · 이상 발생 | 상단 Vehicle Notification(아이콘+제목+한 줄) + 아래 글랜스 2개 |
| 정차 · 상세 | 좌: 이상·점검 목록, 우: 선택 항목 설명(원인, 해야 할 일) + 주 액션 "가까운 블루핸즈 찾기" |
| 정차 · 블루핸즈 | 좌: 주변 블루핸즈 목록(거리, 전화) + "길 안내", 우: "휴대폰으로 예약" QR + 예약에 쓸 정비 항목 요약 |
| 정차 · 리포트 | 안전 점수(큰 숫자), 이벤트 횟수, 팁 1~2개, 기록 삭제 |

## 금지

- 보라색 (Pleos에서 자율주행 전용 `informative_autonomous_driving`)
- 그라디언트, 왼쪽 굵은 테두리 카드, 이모지, 장식용 그림자
- 다크 배경 위 `text-tertiary`보다 어두운 본문
- 틸 `selected`를 글자색으로 사용 (대비 부족)
