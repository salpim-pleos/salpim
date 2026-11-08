plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.salpim.feature.service"
    buildFeatures { compose = true }
}

// NaviHelper 구현에 직접 의존하지 않는다. core/model 의 NavigationLauncher 포트만 본다.
dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
