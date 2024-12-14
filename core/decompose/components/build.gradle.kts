plugins {
    id("ru.vladislavsumin.convention.kmp.android-library")
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
