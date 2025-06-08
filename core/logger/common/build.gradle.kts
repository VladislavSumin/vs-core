plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        jvmTest.dependencies {
            // TODO вынести библиотеки для тестирования в convention плагин
            implementation(vsCoreLibs.testing.mockk)
        }
    }
}
