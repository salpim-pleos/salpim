# 시연용 APK와 영상

| 파일 | 내용 |
|---|---|
| [salpim-release.apk](salpim-release.apk) | 서명된 release APK (minSdk 28). 자동차 기기면 Pleos Vehicle SDK 신호, 아니면 시뮬레이션 신호로 동작 |
| [salpim-demo-simulation.mp4](salpim-demo-simulation.mp4) | 시연 영상 약 2분, 소리 없음. 일반 Android 에뮬레이터(API 34)에서 시뮬레이션 신호로 녹화 |

영상 순서 (화면에 "시뮬레이션 신호"가 항상 표시됨):

1. 주차 — 내 차 상태 (부품 6곳 정상)
2. `engine_oil_warning` — 주행 중 엔진오일 부족: 오일 아이콘이 노랗게 켜지고 "엔진오일이 부족해요 · 점검 권장"
3. `parking_after_warning` — 정차 후 "지금 할 일": 설명, 조치, 블루핸즈 안내·예약 버튼
4. 음성 예시 "가까운 블루핸즈 찾아 줘" → "첫 번째로 가 줘" (장소는 **예시 데이터**, 일반 에뮬레이터에는 Pleos 내비가 없어 길 안내 대신 안내 문구)
5. "휴대폰으로 예약" — 공식 예약 페이지 QR 자리 (QR 렌더링은 준비 중)
6. `low_fuel_trip` — 연료 18%로 장거리 출발: 주행 중 "지금 주유하세요", 정차 후 "가까운 주유소"

Pleos 에뮬레이터에서 시연 시나리오를 쓰려면 `./gradlew assembleRelease -Psalpim.signalSource=sim`으로 빌드합니다.
