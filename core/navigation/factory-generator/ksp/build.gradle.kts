plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.preset.publish")
}

dependencies {
    implementation(projects.core.ksp.utils)
    implementation(projects.core.navigation.factoryGenerator.api)

    testImplementation(kotlin("test"))
    testImplementation(projects.core.navigation.impl)
    testImplementation(vsCoreLibs.kotlin.compileTesting.ksp)
}

// TODO вынести в convention plugin
tasks.test {
    useJUnitPlatform()
}
