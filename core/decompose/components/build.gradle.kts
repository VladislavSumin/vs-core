plugins {
    id("ru.vladislavsumin.convention.kmp.android-library")
    id("ru.vladislavsumin.convention.publication.sonatype")
    `maven-publish`
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.coroutines.core)
            api(libs.decompose.core)

            implementation(libs.kotlin.serialization.core)
            implementation(libs.kotlin.serialization.json)
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core decompose components"
        description = "Core decompose components framework"
    }
}
