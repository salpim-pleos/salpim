plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.salpim.core.ux"
    // AAOS 표준 Car API (CarUxRestrictionsManager). 비자동차 기기에서는 런타임에 없으므로 사용 전 FEATURE_AUTOMOTIVE 확인
    useLibrary("android.car")
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
