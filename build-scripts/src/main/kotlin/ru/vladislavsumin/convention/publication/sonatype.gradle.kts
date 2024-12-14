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

ext["signing.keyId"] = projectConfiguration.core.signing.keyId
ext["signing.password"] = projectConfiguration.core.signing.password
ext["signing.secretKeyRingFile"] = projectConfiguration.core.signing.secretKeyRingFile

signing {
    sign(publishing.publications)
}
