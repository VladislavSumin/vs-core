plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.navigation.api)
            api(projects.core.decompose.components)
            api(projects.core.decompose.compose)

            implementation(projects.core.collections.tree)
            implementation(projects.core.logger.api)

            implementation(vsCoreLibs.kotlin.coroutines.core)
            implementation(vsCoreLibs.kotlin.serialization.json)
        }
    }
}

mavenPublishing {
    pom {
        name = "VS core navigation impl"
        description = "Part of VS core navigation framework"
    }
}
