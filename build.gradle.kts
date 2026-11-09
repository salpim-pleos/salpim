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
    alias(libs.plugins.detekt) apply false
}

// 클린 코드 규칙 (AGENTS.md 7절): 모든 모듈에 detekt + ktlint(formatting). `./gradlew detekt`
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        parallel = true
    }
    dependencies {
        add("detektPlugins", rootProject.libs.detekt.formatting)
    }
}

// 프로젝트 경로에 한글 등 비ASCII 문자가 있으면 빌드 산출물만 ASCII 경로로 뺀다.
// (adb가 비ASCII 경로의 APK를 설치하지 못하고, JVM 테스트 워커도 깨질 수 있음)
// 직접 지정: gradle.properties 또는 -P 로 salpim.buildRoot=<경로>
val externalBuildRoot: File? = providers.gradleProperty("salpim.buildRoot").orNull?.let(::File)
    ?: File(System.getProperty("user.home"), ".salpim-build")
        .takeIf { rootDir.absolutePath.any { it.code > 127 } }

if (externalBuildRoot != null) {
    allprojects {
        val sub = if (this == rootProject) "_root" else path.removePrefix(":").replace(':', '/')
        layout.buildDirectory.set(File(externalBuildRoot, sub))
    }
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
