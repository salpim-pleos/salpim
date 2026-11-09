#!/usr/bin/env bash
# e2e 화면 테스트 (app/src/androidTest)
# - 응답하는 adb 기기/에뮬레이터가 있으면 그 위에서 실행 (Pleos 에뮬레이터 권장)
# - 없으면 Gradle Managed Device(pixel6Api34)를 띄워 실행
# - 강제 선택: E2E_TARGET=connected | managed
set -euo pipefail
cd "$(dirname "$0")/.."

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" && -n "${LOCALAPPDATA:-}" ]]; then SDK="$LOCALAPPDATA/Android/Sdk"; fi
if [[ -z "$SDK" ]]; then SDK="$HOME/Library/Android/sdk"; fi
ADB="$SDK/platform-tools/adb"

has_working_device() {
  [[ -x "$ADB" || -x "$ADB.exe" ]] || return 1
  # "device" 상태라도 명령을 거부하는 가짜 기기(다른 앱이 연 adb 포트 등)는 제외
  local d
  for d in $("$ADB" devices | awk 'NR>1 && $2=="device" {print $1}'); do
    if [[ "$(timeout 10 "$ADB" -s "$d" shell echo ok 2>/dev/null | tr -d '\r')" == "ok" ]]; then
      export ANDROID_SERIAL="$d"
      return 0
    fi
  done
  return 1
}

target="${E2E_TARGET:-auto}"
if [[ "$target" == "auto" ]]; then
  if has_working_device; then target=connected; else target=managed; fi
fi

if [[ "$target" == "connected" ]]; then
  echo "[e2e] 연결된 기기에서 실행 (${ANDROID_SERIAL:-default})"
  ./gradlew connectedDebugAndroidTest "$@"
else
  echo "[e2e] Gradle Managed Device pixel6Api34에서 실행"
  ./gradlew pixel6Api34DebugAndroidTest "$@"
fi
