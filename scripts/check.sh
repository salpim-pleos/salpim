#!/usr/bin/env bash
# 커밋 전 검증: 전체 단위 테스트 + lint
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew testDebugUnitTest lintDebug --continue "$@"
