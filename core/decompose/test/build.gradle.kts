plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.decompose.core)
            implementation(vsCoreLibs.kotlin.coroutines.core)
            implementation(vsCoreLibs.kotlin.coroutines.test)
        }
    }
}
