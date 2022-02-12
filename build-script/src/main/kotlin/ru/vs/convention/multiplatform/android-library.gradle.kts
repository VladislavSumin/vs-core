package ru.vs.convention.multiplatform

plugins {
    id("kotlin-multiplatform")
    id("com.android.library")
    id("ru.vs.convention.android.base")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }
}

android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}
