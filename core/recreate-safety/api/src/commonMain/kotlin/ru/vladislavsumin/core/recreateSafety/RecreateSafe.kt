package ru.vladislavsumin.core.recreateSafety

/**
 * Маркерная аннотация, указывающая что значение данного типа безопасно сохранять
 * во [ViewModel][ru.vladislavsumin.core.decompose.components.ViewModel] при пересоздании компонента
 * (например при смене конфигурации Android).
 *
 * Типы, помеченные этой аннотацией, гарантируют что их экземпляры:
 * - либо переживают пересоздание компонента (хранятся в DI или InstanceKeeper)
 * - либо их значение не зависит от конкретного экземпляра (value-based)
 * - либо они специально спроектированы для безопасного захвата внутри viewModel {} блока
 *
 * Используется IR-плагином компилятора [core/recreate-safety/plugin] для статической проверки:
 * все значения, захваченные лямбдой внутри вызова `viewModel {}`, должны иметь тип,
 * аннотированный [RecreateSafe].
 *
 * Пример объявления safe-типа:
 * ```kotlin
 * @RecreateSafe
 * interface MyService {
 *     fun doSomething()
 * }
 * ```
 *
 * @see ru.vladislavsumin.core.decompose.components.GenericComponent.viewModel
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
public annotation class RecreateSafe
