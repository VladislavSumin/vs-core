plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
    `maven-publish`
}

kotlin {
    explicitApi()
}

mavenPublishing {
    pom {
        name = "VS core collection tree"
        description = "Tree collection"
    }
}
