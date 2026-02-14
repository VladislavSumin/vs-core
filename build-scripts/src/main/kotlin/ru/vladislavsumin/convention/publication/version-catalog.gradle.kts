package ru.vladislavsumin.convention.publication

import org.gradle.kotlin.dsl.`version-catalog`
import ru.vladislavsumin.configuration.projectConfiguration

/**
 * Настраивает публикацию каталога версий
 */

plugins {
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
    id("ru.vladislavsumin.convention.publication.sonatype")
    `version-catalog`
}

catalog {
    versionCatalog {
        version("vs-core", project.projectConfiguration.version)
        from(files("libs.versions.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("versionCatalog") {
            this.artifactId = "vs-core-libs"
            from(components["versionCatalog"])
        }
    }
}
