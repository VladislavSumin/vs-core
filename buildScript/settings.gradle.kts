// TODO убрать когда апи станет стабильным
@file:Suppress("UnstableApiUsage")

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    // TODO обновить политику когда гредл исправит ошибку
    // https://github.com/gradle/gradle/issues/15732
    // repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("coreLibs") {
            from(files("../core-libs.versions.toml"))
        }
    }
}
