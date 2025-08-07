plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation.impl)
            implementation(projects.core.navigation.debug)
            implementation(projects.core.di)
        }
    }
}
