plugins {
    id("ru.vladislavsumin.convention.preset.ksp-code-generator")
    id("ru.vladislavsumin.convention.preset.publish")
}

dependencies {
    implementation(projects.core.navigation.factoryGenerator.api)
    testImplementation(projects.core.navigation.impl)
    testImplementation(projects.core.navigation.compose)
}
