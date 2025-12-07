import ru.vladislavsumin.utils.internalApi

plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.preset.publish")
}

internalApi("ru.vladislavsumin.core.navigation.InternalNavigationApi")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.collections.tree)

            implementation(projects.core.navigation.impl)
            implementation(projects.core.uikit.graph)

            implementation(compose.runtime)
            implementation(compose.material3)
        }
    }
}
