package ru.vladislavsumin.core.factoryGenerator

/**
 * Переносит аргумент из конструктора фабрики в аргументы create.
 * @see GenerateFactory
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class ByCreate
