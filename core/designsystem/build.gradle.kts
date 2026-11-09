plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// 디자인 토큰과 공용 컴포넌트 (docs/DESIGN.md). 화면 코드는 색·글자 크기·간격 수치를 직접 쓰지 않고 여기 값을 쓴다.
android {
    namespace = "com.salpim.core.designsystem"
    buildFeatures { compose = true }
}

dependencies {
    api(project(":core:model"))
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.material3)
}
