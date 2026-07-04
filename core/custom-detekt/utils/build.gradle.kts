plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
}

dependencies {
    compileOnly(vsCoreLibs.detekt.api)
}
