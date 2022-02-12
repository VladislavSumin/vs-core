import ru.vs.build_script.printHelloBuildScript

buildscript {
    dependencies {
        classpath(coreLibs.gradlePlugins.jb.compose)
    }
}

plugins {
    id("ru.vs.convention.detekt-build-scripts")
    id("ru.vs.convention.check-updates")
}

allprojects {
    apply { plugin("ru.vs.convention.detekt") }
}

allprojects {
    val path = mutableListOf<String>()
    var project = this.parent
    while (project != null && project != project.rootProject) {
        path += project.name
        project = project.parent
    }
    val subpackage = path.joinToString(separator = ".")

    group = if (subpackage.isBlank()) "ru.vs"
    else "ru.vs.$subpackage"

    version = "0.1.0"
}

allprojects {
    apply { plugin("ru.vs.convention.maven.publish-to-vs") }
}

printHelloBuildScript("vs-core")
