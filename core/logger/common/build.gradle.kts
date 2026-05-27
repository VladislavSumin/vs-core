plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        jvmTest.dependencies {
            implementation(vsCoreLibs.testing.mockk)
        }
    }
}
