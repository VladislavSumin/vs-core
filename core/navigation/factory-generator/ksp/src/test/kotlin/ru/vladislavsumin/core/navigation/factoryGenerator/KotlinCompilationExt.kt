package ru.vladislavsumin.core.navigation.factoryGenerator

import com.tschuchort.compiletesting.JvmCompilationResult
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

// TODO Вынести в общий утилитарный модуль.
@OptIn(ExperimentalCompilerApi::class)
val JvmCompilationResult.kspSourceFileDirectory: File
    get() = outputDirectory.parentFile.resolve("ksp/sources/kotlin")
