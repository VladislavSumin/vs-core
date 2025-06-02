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
    fun checkNoAdditionalArgsTestScreen() {
        val compilationResult = prepareCompilation(
            TestSources.testScreenParams,
            TestSources.testScreenNoAdditionalArgs,
        )
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.OK)
        val screenFile = compilationResult.kspSourceFileDirectory.listFiles().find { it.name == "TestScreenFactory.kt" }
        assertNotNull(screenFile)
        assertEquals(NO_ADDITIONAL_ARGS_SCREEN_FACTORY, screenFile.readText())
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
    }
}