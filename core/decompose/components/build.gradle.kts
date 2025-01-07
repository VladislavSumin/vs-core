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
        }

        commonTest.dependencies {
            implementation(projects.core.decompose.test)
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core decompose components"
        description = "Core decompose components framework"
    }
}
