package ru.vladislavsumin.core.navigation.factoryGenerator

import com.tschuchort.compiletesting.SourceFile

object TestSources {
    val testScreenParams = SourceFile.kotlin(
        name = "TestScreenParams.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.ScreenParams
            data object TestScreenParams: ScreenParams
        """.trimIndent())

    /**
     * Тестовый экран без всяких дополнительных аргументов и параметров
     */
    val testScreenNoAdditionalArgs = SourceFile.kotlin(
        name = "TestScreen.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.screen.Screen
            import ru.vladislavsumin.core.navigation.screen.ScreenContext
            import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
            import androidx.compose.runtime.Composable
            
            @GenerateScreenFactory
            class TestScreen(context: ScreenContext): Screen(context) {
                @Composable
                override fun Render(modifier: Modifier){}
            }
        """.trimIndent()
    )
}
