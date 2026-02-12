plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(vsCoreLibs.kotlin.coroutines.core)
            implementation(projects.core.di)
        }
    }
}
