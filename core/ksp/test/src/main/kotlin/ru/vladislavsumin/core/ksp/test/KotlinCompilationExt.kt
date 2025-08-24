package ru.vladislavsumin.core.ksp.test

import com.tschuchort.compiletesting.JvmCompilationResult
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
public val JvmCompilationResult.kspSourceFileDirectory: File
    get() = outputDirectory.parentFile.resolve("ksp/sources/kotlin")
