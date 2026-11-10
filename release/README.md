# 시연용 APK와 영상

공모전 심사가 익명이라, 업로드한 계정이 페이지에 표시되는 GitHub Releases 대신 이 폴더에 커밋해 둡니다 (커밋 작성자는 익명).

| 파일 | 내용 |
|---|---|
| [salpim-release.apk](salpim-release.apk) | 서명된 release APK (minSdk 28). 자동차 기기면 Pleos Vehicle SDK 신호, 아니면 시뮬레이션 신호로 동작 |
| [salpim-demo-simulation.mp4](salpim-demo-simulation.mp4) | 시연 영상 약 2분 40초, 소리 없음. 일반 Android 에뮬레이터(API 34)에서 시뮬레이션 신호로 녹화 |

영상 순서 (화면에 "시뮬레이션 신호"가 항상 표시됨):

1. 주차 — 내 차 상태 (부품 6곳 정상)
2. `city_hall_trip` — 서울시청으로 출발. 주행 화면에 "서울시청까지 6.0km", 가는 길에 엔진오일 부족: 오일 아이콘이 노랗게 켜지고 "엔진오일이 부족해요 · 점검 권장"
3. 서울시청 도착·주차 → "지금 할 일": 설명, 조치, 블루핸즈 안내·예약 버튼
4. 음성 예시 "가까운 블루핸즈 찾아 줘" → 광희점 1.8km, 신당점, 황학점 → "첫 번째로 가 줘" (일반 에뮬레이터에는 Pleos 내비가 없어 길 안내 대신 안내 문구)
5. "휴대폰으로 예약" — 공식 예약 페이지 QR 자리 (QR 렌더링은 준비 중)
6. `low_fuel_trip` — 연료 18%로 전주(220km) 출발: 주행 중 "지금 주유하세요", 정차 후 "가까운 주유소"

장소는 서울시청 기준 **샘플 목록**입니다 (실제 주소, 거리는 서울시청에서 직선거리, 영업 여부 미확인 — `feature/service/src/main/assets/nearby_places_sample.json`).

Pleos 에뮬레이터에서 시연 시나리오를 쓰려면 `./gradlew assembleRelease -Psalpim.signalSource=sim`으로 빌드합니다.
