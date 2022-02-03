import ru.vs.build_script.printHelloBuildScript

plugins {
    id("ru.vs.empty_plugin")
}

allprojects {
    group = "ru.vs"
    version = "0.1.0"
}

printHelloBuildScript("vs-core")