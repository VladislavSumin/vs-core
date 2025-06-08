plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.logger.common)
            implementation(projects.core.logger.internal)
        }
    }
}
