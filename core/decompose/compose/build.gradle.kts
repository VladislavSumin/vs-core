plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
    `maven-publish`
}

kotlin {
    explicitApi()
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core decompose compose support"
        description = "Compose support for decompose component"
    }
}
