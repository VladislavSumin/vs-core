package ru.vladislavsumin.core.factoryGenerator

/**
 * Внутренняя маркерная аннотация. Используется для пометки фабрик сгенерированных с помощью [GenerateFactory].
 * В дальнейшем эта информация может быть использована другими KPS процессорами для дополнительной кодогенерации.
 * @hide
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GeneratedFactory
