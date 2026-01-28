package ru.vladislavsumin.core.factoryGenerator

import kotlin.reflect.KClass

/**
 * Генерирует фабрику для основного конструктора класса.
 *
 * Фабрика будет лежать в том же пакете, что и создаваемый ей класс, иметь модификатор internal.
 * Внутри фабрики будет один метод create.
 *
 * Имеет два режима работы:
 *
 * 1) [factoryInterface] не указан, тогда будет создана ClassNameFactory.
 * Все методы аргументы конструктора кроме аргументов помеченных [ByCreate] будут перенесены в конструктор фабрики.
 * Те же аргументы что помечены [ByCreate] будут перенесены в аргументы функции create.
 *
 * 2) [factoryInterface] указан, тогда будет создана FactoryInterfaceImpl фабрика. [ByCreate] в этом случае не
 * учитывается. [factoryInterface] должен иметь один метод create возвращающий совместимый тип. Параметры метода
 * create и конструктора класса сравниваются по имени. Не найденные параметры будут вынесены в конструктор фабрики.
 *
 * @param visibility модификатор видимости у класса сгенерированной фабрики
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GenerateFactory(
    val factoryInterface: KClass<*> = Any::class,
    val visibility: PackageVisibility = PackageVisibility.Internal,
)

public enum class PackageVisibility {
    Public,
    Internal,
}
