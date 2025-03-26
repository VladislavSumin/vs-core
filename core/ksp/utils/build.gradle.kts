plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()
}

dependencies {
    api(vsCoreLibs.kotlin.ksp)
    api(vsCoreLibs.kotlinpoet.core)
    api(vsCoreLibs.kotlinpoet.ksp)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core ksp utils"
        description = "Utils for work with ksp and kotlinpoet"
    }
}
