package ru.vladislavsumin.core.navigation

/**
 * Внутреннее api навигации предназначенное только для использования внутри библиотеки.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
)
public annotation class InternalNavigationApi
