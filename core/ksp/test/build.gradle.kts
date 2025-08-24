plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.preset.publish")
}

dependencies {
    api(vsCoreLibs.kotlin.compileTesting.ksp)
}
