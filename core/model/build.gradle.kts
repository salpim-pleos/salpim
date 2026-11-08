plugins {
    alias(libs.plugins.kotlin.jvm)
}

// 순수 Kotlin. 다른 모듈/Android/Pleos SDK에 의존하지 않는다.
dependencies {
    testImplementation(libs.junit)
}
