plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()

    sourceSets {
        jvmTest.dependencies {
            // TODO вынести библиотеки для тестирования в convention плагин
            implementation(vsCoreLibs.testing.mockk)
        }
    }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name = "VS core logger common"
        description = "Part of VS core logger framework"
    }
}
