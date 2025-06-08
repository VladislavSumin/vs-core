plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.preset.publish")
}

dependencies {
    implementation(projects.core.factoryGenerator.api)
    implementation(projects.core.ksp.utils)
}
