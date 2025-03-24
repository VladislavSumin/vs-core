plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.logger.common)
            implementation(projects.core.logger.internal)
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core logger api"
        description = "Part of VS core logger framework"
    }
}
