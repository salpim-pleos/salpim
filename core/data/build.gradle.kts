plugins {
    alias(libs.plugins.kotlin.jvm)
}

// 로컬 저장소. 외부 전송 없음 (안전/UX 규칙 5).
// TODO: 영속화가 필요해지면 Android 라이브러리로 바꾸고 Room 또는 DataStore 구현 추가
dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
