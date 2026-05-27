package ru.vladislavsumin.core.ksp.utils

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

public inline fun <reified T> KSAnnotated.getAnnotation(): KSAnnotation {
    val annotation = T::class.asClassName()
    return annotations.firstOrNull { it.annotationType.resolve().toClassName() == annotation }
        ?: error("$this not annotated with ${annotation.simpleName}")
}
