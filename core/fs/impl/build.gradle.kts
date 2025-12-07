plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish-off")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.fs.api)
            implementation(projects.core.di)
        }
    }
}
