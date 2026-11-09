plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

// 음성 조작 (AGENTS.md 3절 규칙 3: 음성이 주 조작 방식).
// 명령 해석은 순수 Kotlin(com.salpim.feature.voice), Android STT/TTS 어댑터는 com.salpim.feature.voice.android 에만 둔다.
android {
    namespace = "com.salpim.feature.voice"
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
