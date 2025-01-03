/**
 * Общая для проекта и build-scripts часть settings.gradle.kts
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("vsCoreLibs") {
            from(files("../libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
