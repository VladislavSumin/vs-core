plugins {
    id("ru.vs.convention.kmp.android-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.coroutines.core)
            api(libs.decompose.core)

            implementation(libs.kotlin.serialization.core)
            implementation(libs.kotlin.serialization.json)
        }
    }
}
