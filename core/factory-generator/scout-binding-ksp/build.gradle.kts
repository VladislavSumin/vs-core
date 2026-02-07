plugins {
    id("ru.vladislavsumin.convention.preset.ksp-code-generator")
    id("ru.vladislavsumin.convention.preset.publish")
}

dependencies {
    implementation(projects.core.factoryGenerator.api)
    testImplementation(projects.core.factoryGenerator.ksp)
    testImplementation(vsCoreLibs.scout.core)
}
