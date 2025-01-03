plugins {
    id("ru.vladislavsumin.convention.kmp.android-library")
    id("ru.vladislavsumin.convention.kmp.js")
    id("ru.vladislavsumin.convention.kmp.jvm")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("test"))
            implementation(vsCoreLibs.decompose.core)
            implementation(vsCoreLibs.kotlin.coroutines.core)
            implementation(vsCoreLibs.kotlin.coroutines.test)
        }
    }
}
