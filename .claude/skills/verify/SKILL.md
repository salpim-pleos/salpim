---
name: verify
description: 살핌 커밋 전 검증. 전체 단위 테스트와 lint를 돌리고 결과를 요약한다. 커밋/푸시 전, 또는 "빌드 확인", "테스트 돌려" 요청 시 사용.
---

# verify

1. JDK 17 확인: `"$JAVA_HOME/bin/java" -version`. 17이 아니면 멈추고 사용자에게 JDK 17 경로를 묻는다 (PATH의 Java 8로는 AGP 8이 동작하지 않음).
2. 실행: `scripts/check.sh` (= `./gradlew testDebugUnitTest lintDebug --continue`). 첫 실행은 수 분 걸리니 timeout을 넉넉히(10분) 준다.
3. 결과 요약:
   - 테스트: `*/build/test-results/*/*.xml`, `*/*/build/test-results/*/*.xml`에서 클래스별 tests/failures 집계
   - 실패 시 해당 xml의 `<failure>` 메시지와 첫 스택 줄을 보여 준다
   - lint: 오류가 있으면 빌드 실패. 경고는 `*/build/reports/lint-results-debug.html` 위치만 알려 준다
4. 실패한 상태로 커밋하지 않는다. 고칠 수 없으면 사용자에게 상황을 그대로 보고한다.

## 알려진 함정

- `ClassNotFoundException`으로 JVM 테스트가 전부 실패하면: `gradle.properties`의 `org.gradle.jvmargs`에 `-Dfile.encoding=UTF-8`이 들어갔는지 확인 (한글 경로 + CP949 인코딩 불일치).
- "Your project path contains non-ASCII characters" → `android.overridePathCheck=true` 확인.
