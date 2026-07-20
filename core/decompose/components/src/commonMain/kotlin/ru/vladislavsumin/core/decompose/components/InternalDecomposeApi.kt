package ru.vladislavsumin.core.decompose.components

/**
 * Внутреннее api компонентов decompose предназначенное только для использования внутри библиотеки.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
)
public annotation class InternalDecomposeApi
