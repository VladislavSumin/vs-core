import ru.vladislavsumin.utils.internalApi

plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.preset.publish")

    id("org.jetbrains.kotlin.plugin.serialization")
}

internalApi("ru.vladislavsumin.core.navigation.InternalNavigationApi")

kotlin {
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
