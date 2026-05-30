plugins {
    `kotlin-dsl`
    // Внимание! Менять синхронно с libs.versions.toml
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "ru.vladislavsumin"
version = System.getenv("ru.vs.version") ?: "0.0.1"

ext["signing.keyId"] = System.getenv("ru.vs.signing.keyId")
ext["signing.password"] = System.getenv("ru.vs.signing.password")
ext["signing.secretKeyRingFile"] = System.getenv("ru.vs.signing.secretKeyRingFile")

mavenPublishing {
    publishToMavenCentral(automaticRelease = false)

    // Проверка для возможности выкладывать библиотеки локально без обязательной подписи.
    if (System.getenv("ru.vs.signing.secretKeyRingFile")?.isNotBlank() == true) {
        signAllPublications()
    }

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

dependencies {
    // Мы хотим получать доступ к libs из наших convention плагинов, но гредл на текущий момент не умеет прокидывать
    // version catalogs. Поэтому используем костыль отсюда - https://github.com/gradle/gradle/issues/15383
    api(files(vsCoreLibs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(vsCoreLibs.gradlePlugins.kotlin.core)
    implementation(vsCoreLibs.gradlePlugins.kotlin.ksp)
    implementation(vsCoreLibs.gradlePlugins.kotlin.compose.compiler)
    implementation(vsCoreLibs.gradlePlugins.kotlin.serialization)
    implementation(vsCoreLibs.gradlePlugins.kotlin.binaryValidator)
    implementation(vsCoreLibs.gradlePlugins.jb.compose)
    implementation(vsCoreLibs.gradlePlugins.android)
    implementation(vsCoreLibs.gradlePlugins.detekt)
    implementation(vsCoreLibs.gradlePlugins.checkUpdates)
    implementation(vsCoreLibs.gradlePlugins.kover)
    implementation(vsCoreLibs.gradlePlugins.mavenPublish)
}
