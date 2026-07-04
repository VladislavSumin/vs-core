plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
}

dependencies {
    implementation(projects.core.customDetekt.utils)
    compileOnly(vsCoreLibs.detekt.api)
    runtimeOnly(vsCoreLibs.detekt.cli)
}
