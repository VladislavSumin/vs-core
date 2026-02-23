package ru.vladislavsumin.convention.publication

import com.vanniktech.maven.publish.VersionCatalog
import gradle.kotlin.dsl.accessors._682b3ecf0fec931c9497ddf99a38843e.mavenPublishing
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

mavenPublishing {
    pom {
        name = "VS Version Catalog"
        description = "Gradle version catalog with all vs libs"
    }
}

mavenPublishing {
    configure(VersionCatalog())
}
