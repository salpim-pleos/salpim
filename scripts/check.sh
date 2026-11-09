#!/usr/bin/env bash
# 커밋 전 검증: 정적 분석(detekt + ktlint) + 전체 단위 테스트 + Android lint
# 포맷 자동 수정: ./gradlew detekt --auto-correct
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew detekt testDebugUnitTest lintDebug --continue "$@"
