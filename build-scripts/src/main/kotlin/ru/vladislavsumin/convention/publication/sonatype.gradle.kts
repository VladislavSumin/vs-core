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
            credentials {
                username = projectConfiguration.sonatype.username
                password = projectConfiguration.sonatype.password
            }
        }
        maven {
            name = "sonatypeSnapshot"
            setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = projectConfiguration.sonatype.username
                password = projectConfiguration.sonatype.password
            }
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
