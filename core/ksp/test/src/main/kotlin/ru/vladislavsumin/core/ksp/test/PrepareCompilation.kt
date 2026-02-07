package ru.vladislavsumin.core.ksp.test

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.collections.plus

/**
 * Компилирует переданные файлы, применяя [symbolProcessorProvider].
 * Не проверяет результат компиляции.
 */
@OptIn(ExperimentalCompilerApi::class)
public fun prepareCompilation(
    symbolProcessorProviders: List<SymbolProcessorProvider>,
    vararg sourceFiles: SourceFile,
): JvmCompilationResult {
    return KotlinCompilation().apply {
        useKsp2()
        sources += sourceFiles
        this.symbolProcessorProviders += symbolProcessorProviders
        inheritClassPath = true
    }.compile()
}

@OptIn(ExperimentalCompilerApi::class)
public fun prepareCompilation(
    symbolProcessorProvider: SymbolProcessorProvider,
    vararg sourceFiles: SourceFile,
): JvmCompilationResult {
    return prepareCompilation(listOf(symbolProcessorProvider), *sourceFiles)
}
