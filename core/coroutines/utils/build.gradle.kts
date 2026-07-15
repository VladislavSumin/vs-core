plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(vsCoreLibs.kotlin.coroutines.core)
        }

        commonTest.dependencies {
            implementation(projects.core.coroutines.test)
        }
    }
}
