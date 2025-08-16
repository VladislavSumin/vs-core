package ru.vladislavsumin.core.ksp.utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated

public inline fun <reified T : Annotation> Resolver.processAnnotated(
    noinline block: (KSAnnotated) -> Unit,
): List<KSAnnotated> = processAnnotated(annotationName = T::class.qualifiedName!!, block)

public fun Resolver.processAnnotated(annotationName: String, block: (KSAnnotated) -> Unit): List<KSAnnotated> {
    fun processAnnotated(annotated: KSAnnotated): Boolean {
        return try {
            block(annotated)
            true
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("is not resolvable in the current round of processing") == true) {
                // We have cases when one generated factory using inside another generated factory,
                // for these cases we need to processing sources with more than once iteration
                false
            } else {
                throw e
            }
        }
    }

    return getSymbolsWithAnnotation(annotationName)
        .filterNot(::processAnnotated)
        .toList()
}
