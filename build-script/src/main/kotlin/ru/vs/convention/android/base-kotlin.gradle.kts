package ru.vs.convention.android

import ru.vs.build_script.utils.android
import ru.vs.build_script.utils.kotlinOptions

plugins {
    id("ru.vs.convention.android.base")
    kotlin("android")
}

android {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
