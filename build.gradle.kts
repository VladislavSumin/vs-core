import ru.vladislavsumin.configuration.projectConfiguration

plugins {
    id("ru.vladislavsumin.convention.analyze.detekt-all")
    id("ru.vladislavsumin.convention.analyze.check-updates")
}

val currentJavaVersion = JavaVersion.current().majorVersion
require(currentJavaVersion == projectConfiguration.core.jvmVersion) {
    "Project require java ${projectConfiguration.core.jvmVersion}, but current java version is $currentJavaVersion"
}
