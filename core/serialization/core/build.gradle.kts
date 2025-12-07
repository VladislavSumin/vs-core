plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish-off")
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("ru.vladislavsumin.core.serialization.core.InternalSerializationApi")
        }
        commonMain.dependencies {
            api(vsCoreLibs.kotlin.serialization.core)
            implementation(projects.core.di)
        }
    }
}

apiValidation {
    nonPublicMarkers.add("ru.vladislavsumin.core.serialization.core.InternalSerializationApi")
}
