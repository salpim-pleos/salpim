plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.salpim.feature.coach"
    buildFeatures { compose = true }
}

dependencies {
    api(project(":core:model"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    testImplementation(libs.junit)
    // 시나리오 스크립트로 이벤트 검출 재현성 검증
    testImplementation(project(":core:signal"))
}
