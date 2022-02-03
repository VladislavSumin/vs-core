package ru.vs.convention.android

import ru.vs.build_script.utils.android

@Suppress("MagicNumber")
android {
    setCompileSdkVersion(31)

    defaultConfig {
        minSdk = 30
        targetSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
