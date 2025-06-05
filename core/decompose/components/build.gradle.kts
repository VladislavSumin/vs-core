plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
    `maven-publish`
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(vsCoreLibs.kotlin.coroutines.core)
            api(vsCoreLibs.decompose.core)

            implementation(vsCoreLibs.kotlin.serialization.core)
            implementation(vsCoreLibs.kotlin.serialization.json)

            implementation(projects.core.logger.api)
        }

        commonTest.dependencies {
            implementation(projects.core.decompose.test)
        }
    }
}

mavenPublishing {
    pom {
        name = "VS core decompose components"
        description = "Core decompose components framework"
    }
}
