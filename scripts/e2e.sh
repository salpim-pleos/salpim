#!/usr/bin/env bash
# e2e 화면 테스트 (app/src/androidTest)
# - adb에 기기/에뮬레이터가 연결돼 있으면 그 위에서 실행 (Pleos 에뮬레이터 권장)
# - 없으면 Gradle Managed Device(pixel6Api34)를 띄워 실행
set -euo pipefail
cd "$(dirname "$0")/.."

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" && -n "${LOCALAPPDATA:-}" ]]; then SDK="$LOCALAPPDATA/Android/Sdk"; fi
if [[ -z "$SDK" ]]; then SDK="$HOME/Library/Android/sdk"; fi
ADB="$SDK/platform-tools/adb"

if [[ -x "$ADB" || -x "$ADB.exe" ]] && "$ADB" devices | awk 'NR>1 && $2=="device"' | grep -q .; then
  echo "[e2e] 연결된 기기에서 실행"
  ./gradlew connectedDebugAndroidTest "$@"
else
  echo "[e2e] 연결된 기기 없음 → Gradle Managed Device pixel6Api34"
  ./gradlew pixel6Api34DebugAndroidTest "$@"
fi
