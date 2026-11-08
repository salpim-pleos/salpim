import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// 모든 Android 모듈 공통 설정 (대회 요건: minSdk 28)
subprojects {
    val configureAndroid: Plugin<*>.() -> Unit = {
        extensions.configure<BaseExtension> {
            compileSdkVersion(34)
            defaultConfig {
                minSdk = 28
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
    plugins.withId("com.android.application", configureAndroid)
    plugins.withId("com.android.library", configureAndroid)

    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<KotlinAndroidProjectExtension> { jvmToolchain(17) }
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> { jvmToolchain(17) }
        // 순수 Kotlin 모듈도 `./gradlew testDebugUnitTest` 한 번으로 돌도록 연결
        tasks.register("testDebugUnitTest") { dependsOn("test") }
    }
}
