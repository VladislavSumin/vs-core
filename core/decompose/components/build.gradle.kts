plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(vsCoreLibs.kotlin.coroutines.core)
            api(vsCoreLibs.decompose.core)

            implementation(vsCoreLibs.kotlin.serialization.core)
            implementation(vsCoreLibs.kotlin.serialization.json)
        }

        commonTest.dependencies {
            implementation(projects.core.decompose.test)
        }
    }
}
