plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

// 주유 예측. 판단 로직은 순수 Kotlin (Android 의존 없음) — 화면은 app 모듈에서 그린다.
android {
    namespace = "com.salpim.feature.fuel"
}

dependencies {
    api(project(":core:model"))
    testImplementation(libs.junit)
}
