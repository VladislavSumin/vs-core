plugins {
    id("ru.vladislavsumin.convention.kmp.all")
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.logger.manager)
        }

        jvmMain.dependencies {
            implementation(vsCoreLibs.logging.log4j.api)
            implementation(vsCoreLibs.logging.log4j.core)
        }
    }
}

mavenPublishing {
    pom {
        name = "VS core logger platform"
        description = "Part of VS core logger framework"
    }
}
