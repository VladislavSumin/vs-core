package ru.vladislavsumin.core.navigation.factoryGenerator

import com.tschuchort.compiletesting.SourceFile

object TestSources {
    /**
     * Простые параметры экрана без дополнительных параметров.
     */
    val testScreenParams = SourceFile.kotlin(
        name = "TestScreenParams.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.ScreenParams
            
            data object TestScreenParams: ScreenParams
        """.trimIndent()
    )

    /**
     * Параметры экрана который может принимать события
     */
    val testScreenParamsWithIntent = SourceFile.kotlin(
        name = "TestScreenParams.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.IntentScreenParams
            import ru.vladislavsumin.core.navigation.ScreenIntent
            
            data object TestScreenParams: IntentScreenParams<TestScreenIntent>
            data object TestScreenIntent: ScreenIntent
        """.trimIndent()
    )

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

    /**
     * Тестовый экран с одним дополнительным параметром аргументов экрана
     */
    val testScreenWithScreenParams = SourceFile.kotlin(
        name = "TestScreen.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.screen.Screen
            import ru.vladislavsumin.core.navigation.screen.ScreenContext
            import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
            import androidx.compose.runtime.Composable
            
            @GenerateScreenFactory
            class TestScreen(
                params: TestScreenParams,
                context: ScreenContext,
            ): Screen(context) {
                @Composable
                override fun Render(modifier: Modifier){}
            }
        """.trimIndent()
    )

    /**
     * Тестовый экран с одним дополнительным параметром событий экрана.
     */
    val testScreenWithScreenIntents = SourceFile.kotlin(
        name = "TestScreen.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.screen.Screen
            import ru.vladislavsumin.core.navigation.screen.ScreenContext
            import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
            import androidx.compose.runtime.Composable
            import kotlinx.coroutines.channels.ReceiveChannel
            
            @GenerateScreenFactory
            class TestScreen(
                intents: ReceiveChannel<TestScreenIntent>,
                context: ScreenContext,
            ): Screen(context) {
                @Composable
                override fun Render(modifier: Modifier){}
            }
        """.trimIndent()
    )

    /**
     * Тестовый экран с одним дополнительными параметрами аргументов экрана и событий экрана.
     */
    val testScreenWithScreenParamsAndIntents = SourceFile.kotlin(
        name = "TestScreen.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.screen.Screen
            import ru.vladislavsumin.core.navigation.screen.ScreenContext
            import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
            import androidx.compose.runtime.Composable
            import kotlinx.coroutines.channels.ReceiveChannel
            
            @GenerateScreenFactory
            class TestScreen(
                params: TestScreenParams,
                intents: ReceiveChannel<TestScreenIntent>,
                context: ScreenContext,
            ): Screen(context) {
                @Composable
                override fun Render(modifier: Modifier){}
            }
        """.trimIndent()
    )

    /**
     * Тестовый экран со всеми видами типов.
     */
    val testScreenWithAllData = SourceFile.kotlin(
        name = "TestScreen.kt",
        contents = """
            import ru.vladislavsumin.core.navigation.screen.Screen
            import ru.vladislavsumin.core.navigation.screen.ScreenContext
            import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
            import androidx.compose.runtime.Composable
            import kotlinx.coroutines.channels.ReceiveChannel
            
            @GenerateScreenFactory
            class TestScreen(
                extra: String,
                params: TestScreenParams,
                intents: ReceiveChannel<TestScreenIntent>,
                context: ScreenContext,
            ): Screen(context) {
                @Composable
                override fun Render(modifier: Modifier){}
            }
        """.trimIndent()
    )
}
