plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            api(vsCoreLibs.testing.roborazzi.composeDesktop)
            implementation(compose.foundation)
        }
    }
}
