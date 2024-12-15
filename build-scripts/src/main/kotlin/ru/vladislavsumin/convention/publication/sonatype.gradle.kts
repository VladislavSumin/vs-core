package ru.vladislavsumin.convention.publication

import ru.vladislavsumin.configuration.projectConfiguration

/**
 * Базовые настройки для публикации модуля в sonatype репозиторий.
 */

plugins {
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
    `maven-publish`
    signing
}

val sonatypeDir = "${project.layout.buildDirectory.get().asFile.absolutePath}/sonatype"

publishing {
    repositories {
        maven {
            name = "projectLocal"
            setUrl("file://${sonatypeDir}/localRepository")
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        url = "https://github.com/VladislavSumin/vs-core"

        scm {
            url = "https://github.com/VladislavSumin/vs-core"
        }

        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://github.com/VladislavSumin/vs-core/blob/master/LICENSE"
            }
        }

        developers {
            developer {
                id = "VladislavSumin"
                name = "Vladislav Sumin"
                url = "https://github.com/VladislavSumin"
            }
        }
    }
}

ext["signing.keyId"] = projectConfiguration.signing.keyId
ext["signing.password"] = projectConfiguration.signing.password
ext["signing.secretKeyRingFile"] = projectConfiguration.signing.secretKeyRingFile

signing {
    sign(publishing.publications)
}

// TODO это временное решение до написание нормального плагина для релизов
project.tasks.register<Zip>("zipLocalRepositoryForSonatypeUpload") {
    dependsOn(tasks.named("publishAllPublicationsToProjectLocalRepository"))
    from("$sonatypeDir/localRepository")
    exclude("**/maven-metadata.xml*")
    archiveFileName.set("localRepository.zip")
    destinationDirectory.set(File(sonatypeDir))
}