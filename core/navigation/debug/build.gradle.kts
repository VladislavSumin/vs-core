plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {

    sourceSets {
        all {
            languageSettings.optIn("ru.vladislavsumin.core.navigation.InternalNavigationApi")
        }

        commonMain.dependencies {
            api(projects.core.collections.tree)

            implementation(projects.core.decompose.compose)
            implementation(projects.core.navigation.impl)
            implementation(projects.core.navigation.compose)
            implementation(projects.core.uikit.graph)

            implementation(compose.runtime)
            implementation(compose.material3)
        }
    }
}
