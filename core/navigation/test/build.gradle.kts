import ru.vladislavsumin.utils.internalApi

plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

internalApi("ru.vladislavsumin.core.navigation.InternalNavigationApi")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("test"))
            api(projects.core.navigation.impl)
            implementation(projects.core.decompose.test)
            implementation(vsCoreLibs.decompose.core)
        }
    }
}
