# Navigation Factory Generator

KSP-процессор для автоматической генерации реализаций `ScreenFactory`. Устраняет рутиный бойлерплейт, связанный с
ручным созданием фабрик экранов.

Модуль состоит из двух подмодулей:

- **`api`** — аннотация `@GenerateScreenFactory` (зависимость compile-time)
- **`ksp`** — KSP-процессор, генерирующий код (подключается через `ksp`/`kspCommonMainMetadata`)

## Подключение

```kotlin
plugins {
    id("ru.vladislavsumin.convention.kmp.ksp-hack") // обязательно для KMP
}

dependencies {
    implementation(projects.core.navigation.factoryGenerator.api)
    add("kspCommonMainMetadata", projects.core.navigation.factoryGenerator.ksp)
}
```

## Использование

Аннотируйте класс экрана:

```kotlin
@GenerateScreenFactory
class MyScreen(
    extra: String,
    params: MyScreenParams,
    intents: ReceiveChannel<MyScreenIntent>,
    context: ComponentContext,
) : Screen(context)
```

Будет сгенерирован класс `MyScreenFactory`:

```kotlin
internal class MyScreenFactory(
    private val extra: String,
) : ScreenFactory<ComponentContext, MyScreenParams, MyScreenIntent, MyScreen> {
    override fun create(
        context: ComponentContext,
        params: MyScreenParams,
        intents: ReceiveChannel<MyScreenIntent>,
    ): MyScreen = MyScreen(extra, params, intents, context, )
}
```

Параметры `params`, `intents` и `context` передаются в `create()`, остальные — в конструктор фабрики.

## Требования к аннотируемому классу

1. Класс **должен** наследовать `GenericScreen` (или его тайпалиас `Screen`).
2. Класс **должен** иметь primary constructor.
3. Конструктор должен содержать `context: ScreenContext` (где `ScreenContext` = `GenericComponentContext`).
4. Параметр `params` — либо параметр конструктора типа `IntentScreenParams` с именем `params`, либо класс
   `<ИмяЭкрана>Params` в том же пакете (разрешается автоматически).
5. Параметр `intents` — опциональный `ReceiveChannel<I>`, где `I` = `ScreenIntent`. Должен называться `intents`.
6. Все остальные параметры конструктора становятся параметрами конструктора фабрики.

## Ошибки компиляции

Процессор проверяет корректность использования и выдаёт понятные ошибки:

- Класс не реализует `GenericScreen`
- Отсутствует primary constructor
- Найдено несколько параметров типа `IntentScreenParams`
- Параметр `params` назван иначе
- Автоматически разрешённый класс `<Имя>Params` не найден
