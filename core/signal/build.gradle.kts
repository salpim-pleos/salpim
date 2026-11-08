plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

// Pleos SDK 호출은 이 모듈(과 navi 어댑터)에만 둔다. SDK 타입을 밖으로 노출하지 않는다.
android {
    namespace = "com.salpim.core.signal"
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)
    // TODO(spike-S6): Pleos Vehicle SDK / NaviHelper 의존성 좌표 확정 후 추가
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
