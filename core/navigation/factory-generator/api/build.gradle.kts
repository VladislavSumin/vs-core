plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core navigation ksp api"
        description = "Part of VS core navigation framework"
    }
}
