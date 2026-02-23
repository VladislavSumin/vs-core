package ru.vladislavsumin.convention.publication

import com.android.build.gradle.internal.tasks.factory.dependsOn
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

// publishMavenPublicationToMavenCentralRepository not exists before evaluate.
afterEvaluate {
    val signTask = tasks.named("signVersionCatalogPublication")
    tasks.named("publishMavenPublicationToMavenCentralRepository").dependsOn(signTask)
}
