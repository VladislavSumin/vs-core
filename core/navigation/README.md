# Фреймворк навигации

Декларативный фреймворк навигации для Kotlin Multiplatform, построенный поверх [Decompose](https://github.com/arkivanov/Decompose). Навигация описывается в виде дерева на этапе инициализации приложения, после чего фреймворк автоматически управляет переходами, сохранением состояния и маршрутизацией.

## Ключевые концепции

- **Дерево навигации** — на старте приложения все экраны и хосты навигации регистрируются в виде дерева. Это позволяет фреймворку знать, где открыть экран при вызове `navigator.open()`.
- **`IntentScreenParams`** — параметры запуска экрана. Класс параметров является ключом экрана в графе навигации, а инстанс — ключом конкретного экземпляра (по `equals`). Для экранов без параметров используется `ScreenParams` и `data object`.
- **`ScreenIntent`** — события, которые можно передать уже открытому экрану. Если экран не поддерживает события, используется `NoIntent`.
- **`ScreenFactory`** — фабрика для создания экземпляров экранов. Может генерироваться автоматически через KSP (см. `factory-generator`).
- **`NavigationViewModel`** — ViewModel с поддержкой навигации. Позволяет вызывать `open()`/`close()` из ViewModel без явной связки.

## Типы навигации

| Тип | Описание |
|---|---|
| `childNavigationStack` | Стек экранов. Несколько экранов одновременно, виден только верхний. |
| `childNavigationSlot` | Слот для одного экрана. При открытии нового предыдущий закрывается. |
| `childNavigationPages` | Страницы (вкладки). Несколько экранов, активен любой из них. |

## Использование

### 1. Определение параметров экрана

```kotlin
data object MainScreenParams : ScreenParams
data class ProfileScreenParams(val id: String) : ScreenParams
```

### 2. Регистрация навигации

Реализуйте `NavigationRegistrar` и зарегистрируйте экраны и хосты:

```kotlin
class AppNavigationRegistrar : NavigationRegistrar {
    override fun NavigationRegistry.register() {
        registerScreen(
            factory = MainScreenFactory(),
            defaultParams = MainScreenParams,
        ) {
            MainHost opens setOf(ProfileScreenParams::class)
        }

        registerScreen(
            factory = ProfileScreenFactory(),
        ) {
            ProfileHost opens setOf(/* ... */)
        }
    }
}
```

### 3. Создание навигации

Без DI:

```kotlin
val navigation = Navigation(setOf(AppNavigationRegistrar()))
```

С DI (Kodein):

```kotlin
bindGenericNavigation { AppNavigationRegistrar() }
```

### 4. Корневой компонент

```kotlin
class RootComponent(
    context: ComponentContext,
    private val navigation: Navigation,
) : Component(context), ComposeComponent {

    private val root = childNavigationRoot(
        navigation = navigation,
        onContentReady = { /* скрыть splash */ },
    )

    @Composable
    override fun render() {
        root.render()
    }
}
```

### 5. Навигация внутри экранов

```kotlin
class MainScreen(context: ComponentContext) : Screen(context) {
    fun onProfileClick() {
        navigator.open(ProfileScreenParams(id = "42"))
    }
}
```

### 6. Навигация из ViewModel

```kotlin
class ProfileViewModel : NavigationViewModel() {
    fun onLogout() {
        open(MainScreenParams)
        close(ProfileScreenParams(id = "42"))
    }
}
```

### 7. Хосты навигации внутри экранов

```kotlin
class MainScreen(context: ComponentContext) : Screen(context) {
    private val stack = childNavigationStack(
        navigationHost = MainHost,
        defaultStack = { listOf(MainScreenParams) },
    )

    @Composable
    override fun render() {
        Children(stack) { child ->
            child.instance.render()
        }
    }
}
```

## Задержка splash-экрана

`GenericScreen.delaySplashScreen()` позволяет искусственно задержать splash-экран на время загрузки данных. Вызывается только при холодном старте или восстановлении состояния, но не при обычных переходах.

## Сохранение состояния

Все типы навигации поддерживают автоматическое сохранение и восстановление состояния через Essenty StateKeeper. Отключается параметром `allowStateSave = false`.

## Предназначение модулей

- **`api`** — базовые интерфейсы: `NavigationHost`, `IntentScreenParams`, `ScreenParams`, `ScreenIntent`, `InternalNavigationApi`. Подключается везде, где нужно объявить параметры экрана.
- **`impl`** — реализация фреймворка: `Navigation`, `Screen`, `ScreenFactory`, навигационные хосты, сериализация, навигаторы. Основная зависимость для работы навигации.
- **`di`** — интеграция с Kodein DI: модуль `Modules.coreNavigation()`, расширение `bindGenericNavigation()`.
- **`debug`** — отладочные инструменты: UML-диаграмма графа навигации для визуализации в debug-сборках.
- **`factory-generator`** — KSP-процессор для автогенерации `ScreenFactory`. См. [отдельный README](factory-generator/README.md).
