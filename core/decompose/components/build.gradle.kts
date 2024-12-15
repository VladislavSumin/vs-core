plugins {
    id("ru.vladislavsumin.convention.kmp.android-library")
    id("ru.vladislavsumin.convention.publication.sonatype")
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
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core decompose components"
        description = "Core decompose components framework"
    }
}
