plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()
}

mavenPublishing {
    pom {
        name = "VS core factory generator api"
        description = "Part of VS core factory-generator"
    }
}
