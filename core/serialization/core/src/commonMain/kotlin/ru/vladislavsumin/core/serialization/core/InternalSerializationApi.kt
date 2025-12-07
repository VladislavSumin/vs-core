package ru.vladislavsumin.core.serialization.core

/**
 * Внутреннее api предназначенное только для использования внутри библиотеки.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
)
internal annotation class InternalSerializationApi
