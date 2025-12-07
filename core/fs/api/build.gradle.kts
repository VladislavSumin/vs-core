plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    // TODO FS not ready for publish now
    // id("ru.vladislavsumin.convention.preset.publish")
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(vsCoreLibs.kotlin.io.core)
        }
    }
}
