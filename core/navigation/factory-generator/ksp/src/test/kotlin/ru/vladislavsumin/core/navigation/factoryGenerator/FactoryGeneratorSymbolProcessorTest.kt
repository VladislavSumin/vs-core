package ru.vladislavsumin.core.navigation.factoryGenerator

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCompilerApi::class)
class FactoryGeneratorSymbolProcessorTest {
    @Test
    fun testScreenNoAdditionalArgs() = assertScreenFactorySuccess(
        screen = TestSources.testScreenNoAdditionalArgs,
        factory = NO_ADDITIONAL_ARGS_SCREEN_FACTORY,
    )

    @Test
    fun testScreenWithScreenParams() = assertScreenFactorySuccess(
        screen = TestSources.testScreenWithScreenParams,
        factory = SCREEN_PARAMSL_ARGS_SCREEN_FACTORY,
    )

    fun assertScreenFactorySuccess(screen: SourceFile, factory: String) {
        val compilationResult = prepareCompilation(TestSources.testScreenParams, screen)
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.OK)
        val screenFile = compilationResult.kspSourceFileDirectory.listFiles().find { it.name == "TestScreenFactory.kt" }
        assertNotNull(screenFile)
        assertEquals(factory, screenFile.readText())
    }

    /**
     * Компилирует переданные файлы, применяя [FactoryGeneratorSymbolProcessorProvider].
     * Не проверяет результат компиляции.
     */
    fun prepareCompilation(
        vararg sourceFiles: SourceFile,
    ): KotlinCompilation.Result {
        return KotlinCompilation().apply {
            sources += sourceFiles
            symbolProcessorProviders += FactoryGeneratorSymbolProcessorProvider()
            inheritClassPath = true
        }.compile()
    }


    companion object {
        private val NO_ADDITIONAL_ARGS_SCREEN_FACTORY = """
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory() : ScreenFactory<TestScreenParams, NoIntent, TestScreen> {
  override fun create(
    context: ScreenContext,
    params: TestScreenParams,
    intents: ReceiveChannel<NoIntent>,
  ): TestScreen = TestScreen(context, )
}

        """.trimIndent()

        private val SCREEN_PARAMSL_ARGS_SCREEN_FACTORY = """
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory() : ScreenFactory<TestScreenParams, NoIntent, TestScreen> {
  override fun create(
    context: ScreenContext,
    params: TestScreenParams,
    intents: ReceiveChannel<NoIntent>,
  ): TestScreen = TestScreen(params, context, )
}

        """.trimIndent()
    }
}