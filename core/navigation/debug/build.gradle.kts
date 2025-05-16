plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()

    sourceSets {
        all {
            languageSettings.optIn("ru.vladislavsumin.core.navigation.InternalNavigationApi")
        }

        commonMain.dependencies {
            api(projects.core.collections.tree)

            implementation(projects.core.navigation.impl)
            implementation(projects.core.uikit.graph)

            implementation(compose.runtime)
            implementation(compose.material3)
        }
    }
}

mavenPublishing {
    pom {
        name = "VS core navigation debug"
        description = "Part of VS core navigation framework"
    }
}
