// TODO убрать когда апи станет стабильным
@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("buildScript")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

enableFeaturePreview("VERSION_CATALOGS")
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

include(":coroutines")
