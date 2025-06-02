package ru.vladislavsumin.core.navigation.factoryGenerator

import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

// TODO Вынести в общий утилитарный модуль.
@OptIn(ExperimentalCompilerApi::class)
val KotlinCompilation.Result.kspSourceFileDirectory: File
    get() = outputDirectory.parentFile.resolve("ksp/sources/kotlin")
