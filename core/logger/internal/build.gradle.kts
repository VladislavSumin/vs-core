plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.logger.common)
        }
    }
}
