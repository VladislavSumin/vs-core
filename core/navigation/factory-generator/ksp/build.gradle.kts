plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(projects.core.ksp.utils)
    implementation(projects.core.navigation.factoryGenerator.api)
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core navigation ksp impl"
        description = "Part of VS core navigation framework"
    }
}
