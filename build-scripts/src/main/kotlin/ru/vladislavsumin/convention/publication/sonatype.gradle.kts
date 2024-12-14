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

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/")
        }
        maven {
            name = "sonatypeSnapshot"
            setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

ext["signing.keyId"] = projectConfiguration.core.signing.keyId
ext["signing.password"] = projectConfiguration.core.signing.password
ext["signing.secretKeyRingFile"] = projectConfiguration.core.signing.secretKeyRingFile

signing {
    sign(publishing.publications)
}
