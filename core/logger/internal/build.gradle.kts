plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.logger.common)
        }
    }
}

mavenPublishing {
    pom {
        name = "VS core logger internal"
        description = "Part of VS core logger framework"
    }
}
