plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.salpim.feature.coach"
}

dependencies {
    api(project(":core:model"))
    testImplementation(libs.junit)
    // 시나리오 스크립트로 이벤트 검출 재현성 검증
    testImplementation(project(":core:signal"))
}
