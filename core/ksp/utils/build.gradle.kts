plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.preset.publish")
}

dependencies {
    api(vsCoreLibs.kotlin.ksp)
    api(vsCoreLibs.kotlinpoet.core)
    api(vsCoreLibs.kotlinpoet.ksp)
}
