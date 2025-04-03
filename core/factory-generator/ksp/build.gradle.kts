plugins {
    kotlin("jvm")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(projects.core.factoryGenerator.api)
    implementation(projects.core.ksp.utils)
}

mavenPublishing {
    pom {
        name = "VS core factory generator ksp"
        description = "Part of VS core factory-generator"
    }
}
