plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    // TODO FS not ready for publish now
    // id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(vsCoreLibs.kotlin.io.core)
        }
    }
}
