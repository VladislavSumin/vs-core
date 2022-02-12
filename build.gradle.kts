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

val pBuildNumber: String by project
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

    version = "0.0.1.$pBuildNumber"
}

allprojects {
    apply { plugin("ru.vs.convention.maven.publish-to-vs") }
}

tasks.register("ci") {
    dependsOn(":core:compose:publishAllPublicationsToMavenRepository")
    dependsOn(":core:coroutines:publishAllPublicationsToMavenRepository")
    dependsOn(":core:decompose:publishAllPublicationsToMavenRepository")
    dependsOn(":core:di:publishAllPublicationsToMavenRepository")
    dependsOn(":core:ktor-client:publishAllPublicationsToMavenRepository")
    dependsOn(":core:ktor-server:publishAllPublicationsToMavenRepository")
    dependsOn(":core:logging:publishAllPublicationsToMavenRepository")
    dependsOn(":core:logging-slf4j:publishAllPublicationsToMavenRepository")
    dependsOn(":core:navigation:publishAllPublicationsToMavenRepository")
    dependsOn(":core:serialization:publishAllPublicationsToMavenRepository")
    dependsOn(":core:uikit:publishAllPublicationsToMavenRepository")
    dependsOn(":core:utils:publishAllPublicationsToMavenRepository")
}
