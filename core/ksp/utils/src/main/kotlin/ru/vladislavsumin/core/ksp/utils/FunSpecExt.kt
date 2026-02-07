package ru.vladislavsumin.core.ksp.utils

import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Записывает [FunSpec] в файл создавая [FileSpec] по умолчанию.
 * Удобно когда в файл нужно записать только одну функцию.
 */
public fun FunSpec.writeTo(
    codeGenerator: CodeGenerator,
    packageName: String,
    fileName: String,
    aggregating: Boolean = true,
) {
    FileSpec.builder(
        packageName,
        fileName,
    )
        .addFunction(this)
        .build()
        .writeTo(codeGenerator, aggregating)
}
