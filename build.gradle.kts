import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.registerExternalModuleDetektTask

plugins {
    id("ru.vladislavsumin.convention.analyze.detekt-all")
    id("ru.vladislavsumin.convention.analyze.check-updates")
    id("ru.vladislavsumin.convention.publication.version-catalog")
}

val currentJavaVersion = JavaVersion.current().majorVersion
require(currentJavaVersion == projectConfiguration.core.jvmVersion) {
    "Project require java ${projectConfiguration.core.jvmVersion}, but current java version is $currentJavaVersion"
}

registerExternalModuleDetektTask(
    taskName = "detektBuildScripts",
    moduleDir = projectDir.resolve("build-scripts"),
)
