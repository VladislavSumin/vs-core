import ru.vladislavsumin.utils.internalApi

plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

internalApi("ru.vladislavsumin.core.serialization.core.InternalSerializationApi")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.serialization.core)
            api(vsCoreLibs.kotlin.serialization.yaml)
            implementation(projects.core.di)
        }
    }
}
