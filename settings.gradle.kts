pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // TODO(spike-S6): Pleos SDK 저장소/좌표 확정 후 추가
    }
}

rootProject.name = "salpim"

include(":app")
include(":core:model", ":core:state", ":core:signal", ":core:ux", ":core:data", ":core:designsystem")
include(":feature:warning", ":feature:service", ":feature:coach", ":feature:fuel", ":feature:voice")
