package ru.vladislavsumin.core.decompose.components

private var counter = 0

class TestViewModel : ViewModel() {
    val testSaveableFlow = saveableStateFlow("KEY") { counter++ }
}
