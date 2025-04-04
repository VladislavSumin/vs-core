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

mavenPublishing {
    pom {
        name = "VS core logger manager"
        description = "Part of VS core logger framework"
    }
}
