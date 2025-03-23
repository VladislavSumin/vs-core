plugins {
    `kotlin-dsl`
}

group = "ru.vladislavsumin"

dependencies {
    // Мы хотим получать доступ к libs из наших convention плагинов, но гредл на текущий момент не умеет прокидывать
    // version catalogs. Поэтому используем костыль отсюда - https://github.com/gradle/gradle/issues/15383
    implementation(files(vsCoreLibs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(vsCoreLibs.gradlePlugins.kotlin.core)
    implementation(vsCoreLibs.gradlePlugins.kotlin.compose.compiler)
    implementation(vsCoreLibs.gradlePlugins.kotlin.binaryValidator)
    implementation(vsCoreLibs.gradlePlugins.jb.compose)
    implementation(vsCoreLibs.gradlePlugins.android)
    implementation(vsCoreLibs.gradlePlugins.detekt)
    implementation(vsCoreLibs.gradlePlugins.checkUpdates)
}
