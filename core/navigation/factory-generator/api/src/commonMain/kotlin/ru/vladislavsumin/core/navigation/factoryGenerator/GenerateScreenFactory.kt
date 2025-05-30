package ru.vladislavsumin.core.navigation.factoryGenerator

/**
 * Генерирует фабрику ScreenFactory для любого аннотированного экрана. Требования к аннотируемому классу:
 * 1) Класс должен наследоваться от Screen.
 * 2) У класса должен быть primary constructor.
 * 3) В конструкторе должны ожидаться IntentScreenParams с которыми работает этот экран или эти параметры должны лежать
 *    в том же пакете, что и экран и иметь название класса экрана + Params, тогда генератор сможет найти их
 *    автоматически
 * 4) В конструкторе может присутствовать ReceiveChannel<T>, где T это тип ScreenIntent ожидаемый этим экраном.
 *    Данный параметр обязан называться intents.
 * 5) В конструкторе должен обязательно присутствовать context: ScreenContext.
 * 6) В конструкторе могут присутствовать любые другие параметры, они будут взяты из конструктора фабрики.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GenerateScreenFactory
