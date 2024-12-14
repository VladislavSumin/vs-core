plugins {
    id("ru.vladislavsumin.convention.kmp.android-library")
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
    `maven-publish`
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.coroutines.core)
            api(libs.decompose.core)

            implementation(libs.kotlin.serialization.core)
            implementation(libs.kotlin.serialization.json)
        }
    }
}
