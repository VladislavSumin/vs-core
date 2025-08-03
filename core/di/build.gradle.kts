plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(vsCoreLibs.kodein.core)
        }
        androidMain.dependencies {
            api(vsCoreLibs.kodein.android)
        }
    }
}
