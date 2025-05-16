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
            implementation(compose.runtime)
            // TODO Завязка на материал тут только ради одного цветового токена, возможно стоит от нее отказаться.
            implementation(compose.material3)
            implementation(projects.core.collections.tree)
        }
    }
}

mavenPublishing {
    pom {
        name = "VS core uikit graph"
        description = "Compose views for render graphs"
    }
}
