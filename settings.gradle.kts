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
        // Pleos SDK [확인됨: S6]
        maven("https://nexus-playground.pleos.ai/repository/maven-releases/")
    }
}

rootProject.name = "salpim"

include(":app")
include(":core:model", ":core:state", ":core:signal", ":core:ux", ":core:data", ":core:designsystem")
include(":feature:warning", ":feature:service", ":feature:coach", ":feature:fuel", ":feature:voice")
