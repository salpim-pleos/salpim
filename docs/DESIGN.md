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

## 경고 점등과 예외 상태 (2026-11-09)

- **점등**: 경고가 처음 켜질 때만 600ms ease-out으로 밝아지며 켜진다(1회). 반복 깜빡임·맥동 없음 (Pleos: 주행 중 반복 애니메이션 금지, 애니메이션은 상태 변화 설명용). 켜진 뒤에는 은은한 빛 번짐(glow)으로 유지. `prefers-reduced-motion`/시스템 동작 줄이기면 바로 표시
- **아이콘**: 부품 아이콘은 단순한 외곽선(오일통 등). 실제 계기판 경고등과 똑같이 그리지 않는다
- **색 단계**: 노랑(점검 권장) → 빨강(즉시 정차)만. 빨강 단계에서만 앱이 먼저 한 줄 말한다
- **예외 상태 화면**: 음성 기다림/듣는 중/말하는 중/못 알아들음, 인터넷 없음(내비 검색 화면으로 폴백), 주유 기록 부족(대략값 + 기록 n/5)
- **크기**: 주행 화면 글자 최소 24, 터치 영역 최소 76 (음성 예시 칩 포함)

## 개정 2 (2026-11-09, 사용자 피드백)

1. **음성 우선**: 모든 화면 하단에 음성 바 — 마이크(76dp, 네이비 원), "이렇게 말해 보세요" 예시 문장 칩. 듣는 중에는 마이크가 파랑(`notice`)으로. 앱의 대답은 스피커 아이콘 + 인용문으로 화면에도 남긴다
2. **휑하지 않게**: 내비 옆 1:1 분할(앱 640 폭)을 기본으로 설계. 빈 여백 대신 **내 차 그림**(위에서 본 모습) — 문제 부위만 상태 색으로 칠하고 나머지는 회색
3. **차를 몰라도**: 부품 이름 + 한 줄 쉬운 설명("브레이크액: 차를 세우는 힘"), 차 그림에서 위치를 선으로 가리킴. 정차 화면은 "지금 할 일" 하나를 크게, 버튼은 그 일에 바로 연결
4. 음성으로 지점을 고르게 할 때는 번호를 붙인다 ("첫 번째로 가 줘") — 번호가 실제 선택 수단이라 장식이 아님

아래 "시각 아이디어"의 도로 눈금자는 주행 화면의 진행 막대("다음 점검까지")로 축소 반영.

## 시각 아이디어 (frontend-design 스킬 적용, 2026-11-09)

[anthropics/skills `frontend-design`](https://github.com/anthropics/skills) (Apache-2.0) 절차대로 계획 → 브리프 대비 검토 → 제작 → 자기 비평을 거쳤다.

- **소재에서 출발**: 정비는 km로 잰다. 엔진오일은 딥스틱으로 본다. 하루 운전은 시간 위의 선이다.
- **강조는 한 곳**: 주행 화면의 **도로 눈금자** — "지금"에서 앞으로 남은 거리 위에 다음 점검 지점을 표시. 나머지는 조용하게.
- **딥스틱 4단 눈금**: 오일 레벨은 API 값(`CRITICALLY_LOW`/`LOW`/`NORMAL`/`HIGH`) 그대로 "매우 부족/부족/정상/많음" 칸에 표시. 수치를 지어내지 않는다.
- **리포트는 하루 타임라인**: 주행 구간을 선으로, 급제동·급가속은 그 위 눈금으로.
- **라벨 대신 문장**: "차는 괜찮아요", "오늘은 83점이에요", "2,400km 남았어요".

제거한 AI 기본값: 큰 숫자 + 작은 라벨 반복, 값마다 붙는 라벨, 가운뎃점으로 이은 메타 문자열(`A · B`), 자간을 벌린 대문자 라벨(`SIMULATED` → "시뮬레이션 신호"), 둥근 회색 카드.

## 원칙

0. **문구**: 문장형, 능동태, 버튼은 일어나는 일 그대로("블루핸즈 찾기", "길 안내", "기록 지우기"). 숫자는 tabular-nums.
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
| 주행 · 평상시 | 상태 문장(Headline 60) + 점수 한 줄 + 하단 도로 눈금자(다음 점검 지점, 남은 km) |
| 주행 · 이상 발생 | 문장 "엔진오일이 부족해요" + 한 줄 할 일 + 딥스틱 4단 눈금 + 점수 한 줄 |
| 정차 · 상세 | 좌: 확인할 것 목록(문장형), 우: 설명 문단, 할 일 순서(실제 순서라 번호), 주 액션 "블루핸즈 찾기" + 보조 "휴대폰으로 예약" |
| 정차 · 블루핸즈 | 좌: 거리(큰 숫자, 고르는 기준)로 시작하는 지점 행 + "길 안내", 우: 예약 QR과 한 문장 안내 |
| 정차 · 리포트 | 점수 문장 + 하루 타임라인(주행 구간과 이벤트 눈금) + 팁 문장 2개 + "기록 지우기" |

## 금지

- 보라색 (Pleos에서 자율주행 전용 `informative_autonomous_driving`)
- 그라디언트, 왼쪽 굵은 테두리 카드, 이모지, 장식용 그림자
- 다크 배경 위 `text-tertiary`보다 어두운 본문
- 틸 `selected`를 글자색으로 사용 (대비 부족)
