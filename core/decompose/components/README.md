# Decompose Components

Предоставляет базовую реализацию MVVM для использования поверх библиотеки decompose.

## Пример использования

ViewModel:

```kotlin
internal class MyViewModel : ViewModel() {
    // код вью модели
}
```

Component:

```kotlin
internal class MyComponent(context: ComponentContext) : Component(context) {
    // Создайте вашу view model через специальную функцию.
    // Вью модель будет связана с жизненным циклом компонента, будет переживать пересоздание
    // иерархии компонентов, а так же будет корректно уничтожена при закрытии компонента.
    private val viewModel = viewModel {
        // В реальном коде вы можете передать фабрику для создания вашей ViewModel через конструктор компонента.
        MyViewModel()
    }
}
```

## Сохранение состояния внутри ViewModel

Если вам необходимо сохранять состояние внутри `ViewModel` на случай смерти процесса (аналогично `savedStateHandle`), то
вы можете воспользоваться встроенными возможностями `ViewModel`:
```kotlin
internal class MyViewModel : ViewModel() {
    // Значение данного параметра будет сохранено при смерти процесса.
    private val persistentState: StateFlow<String> = saveableStateFlow(key = "<unique_key>") { "initialValue" }
}
```