import com.android.build.api.dsl.ManagedVirtualDevice
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// release 서명: keystore와 비밀번호는 레포 밖 properties 파일에서 읽는다 (커밋 금지).
// 위치: -Psalpim.signingProps=<경로>, 기본 ~/.salpim-keys/keystore.properties
// 키: storeFile, storePassword, keyAlias, keyPassword. 파일이 없으면 release는 서명 없이 빌드된다 (다른 PC/CI용)
val defaultSigningPropsPath = "${System.getProperty("user.home")}/.salpim-keys/keystore.properties"
val signingPropsFile = file(providers.gradleProperty("salpim.signingProps").getOrElse(defaultSigningPropsPath))
val signingProps: Properties? = signingPropsFile.takeIf { it.isFile }?.let { f ->
    Properties().apply { f.inputStream().use(::load) }
}

android {
    namespace = "com.salpim.app"

    signingConfigs {
        if (signingProps != null) {
            create("release") {
                storeFile = file(signingProps.getProperty("storeFile"))
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.salpim.app"
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        // 신호원: auto(자동차 기기면 Pleos SDK, 아니면 시뮬레이션) | pleos | sim. 예: ./gradlew assembleDebug -Psalpim.signalSource=sim
        val signalSource = providers.gradleProperty("salpim.signalSource").getOrElse("auto")
        buildConfigField("String", "SIGNAL_SOURCE", "\"$signalSource\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }

    testOptions {
        animationsDisabled = true
        // e2e: 연결된 기기 없이도 `./gradlew pixel6Api34DebugAndroidTest`로 실행 가능 (첫 실행 시 이미지 다운로드)
        managedDevices {
            devices {
                maybeCreate<ManagedVirtualDevice>("pixel6Api34").apply {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "google"
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:state"))
    implementation(project(":core:signal"))
    implementation(project(":core:ux"))
    implementation(project(":core:data"))
    implementation(project(":feature:warning"))
    implementation(project(":feature:service"))
    implementation(project(":feature:coach"))
    implementation(project(":feature:fuel"))
    implementation(project(":feature:voice"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(project(":core:designsystem"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
