package ru.vladislavsumin.convention.publication

import com.vanniktech.maven.publish.SonatypeHost
import ru.vladislavsumin.configuration.projectConfiguration

/**
 * Базовые настройки для публикации модуля в sonatype репозиторий.
 */

plugins {
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
    id("com.vanniktech.maven.publish")
}

ext["signing.keyId"] = projectConfiguration.signing.keyId
ext["signing.password"] = projectConfiguration.signing.password
ext["signing.secretKeyRingFile"] = projectConfiguration.signing.secretKeyRingFile

ext["mavenCentralUsername"] = projectConfiguration.sonatype.username
ext["mavenCentralPassword"] = projectConfiguration.sonatype.password


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
    signAllPublications()

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
