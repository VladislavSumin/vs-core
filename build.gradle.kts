import ru.vs.build_script.printHelloBuildScript

buildscript {
    dependencies {
        classpath(coreLibs.gradlePlugins.jb.compose)
    }
}

plugins {
    id("ru.vs.empty_plugin")
}

allprojects {
    group = "ru.vs"
    version = "0.1.0"
}

printHelloBuildScript("vs-core")