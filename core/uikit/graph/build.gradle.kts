plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.preset.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            // TODO Завязка на материал тут только ради одного цветового токена, возможно стоит от нее отказаться.
            implementation(compose.material3)
            implementation(projects.core.collections.tree)
        }
    }
}
