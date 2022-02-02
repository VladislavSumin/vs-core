plugins {
    `kotlin-dsl`
}

dependencies {
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("EmptyPlugin") {
            id = "empty_plugin"
            implementationClass = "ru.vs.build_script.EmptyPlugin"
        }
    }
}
