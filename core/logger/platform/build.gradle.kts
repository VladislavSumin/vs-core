plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.logger.manager)
        }

        jvmMain.dependencies {
            implementation(vsCoreLibs.logging.log4j.api)
            implementation(vsCoreLibs.logging.log4j.core)
        }
    }
}
