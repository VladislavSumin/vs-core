import ru.vladislavsumin.utils.internalApi

plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.preset.publish")
}

internalApi("ru.vladislavsumin.core.decompose.components.InternalDecomposeApi")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("test"))
            implementation(vsCoreLibs.decompose.core)
            api(projects.core.decompose.components)
        }
    }
}
