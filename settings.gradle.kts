// TODO убрать когда апи станет стабильным
@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-script")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("coreLibs") {
            from(files("core-libs.versions.toml"))
        }
    }
}

rootProject.name = "vs-core"

include(
    ":core:compose",
    ":core:coroutines",
    ":core:decompose",
    ":core:di",
    ":core:ktor-client",
    ":core:ktor-server",
    ":core:logging",
    ":core:logging-slf4j",
    ":core:navigation",
    ":core:serialization",
    ":core:uikit",
    ":core:utils",
)
