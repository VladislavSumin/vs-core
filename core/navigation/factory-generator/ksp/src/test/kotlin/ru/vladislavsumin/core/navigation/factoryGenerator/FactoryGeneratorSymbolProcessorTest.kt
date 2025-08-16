package ru.vladislavsumin.core.navigation.factoryGenerator

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCompilerApi::class)
class FactoryGeneratorSymbolProcessorTest {
    @Test
    fun testScreenNoAdditionalArgs() = assertScreenFactorySuccess(
        screenParams = TestSources.testScreenParams,
        screen = TestSources.testScreenNoAdditionalArgs,
        factory = NO_ADDITIONAL_ARGS_SCREEN_FACTORY,
    )

    @Test
    fun testScreenWithScreenParams() = assertScreenFactorySuccess(
        screenParams = TestSources.testScreenParams,
        screen = TestSources.testScreenWithScreenParams,
        factory = SCREEN_PARAMS_ARGS_SCREEN_FACTORY,
    )

    @Test
    fun testScreenWithScreenIntents() = assertScreenFactorySuccess(
        screenParams = TestSources.testScreenParamsWithIntent,
        screen = TestSources.testScreenWithScreenIntents,
        factory = SCREEN_INTENTS_ARGS_SCREEN_FACTORY,
    )

    @Test
    fun testScreenWithScreenParamsAndIntents() = assertScreenFactorySuccess(
        screenParams = TestSources.testScreenParamsWithIntent,
        screen = TestSources.testScreenWithScreenParamsAndIntents,
        factory = SCREEN_PARAMS_AND_INTENTS_ARGS_SCREEN_FACTORY,
    )

    @Test
    fun testScreenWithAllData() = assertScreenFactorySuccess(
        screenParams = TestSources.testScreenParamsWithIntent,
        screen = TestSources.testScreenWithAllData,
        factory = ALL_ARGS_SCREEN_FACTORY,
    )

    @Test
    fun testScreenWithGenericContext() = assertScreenFactorySuccess(
        screenParams = TestSources.testScreenParams,
        screen = TestSources.testScreenWithCustomContext,
        factory = CUSTOM_CONTEXT_SCREEN_FACTORY,
    )

    @Test
    fun testAliasScreenWithGenericContext() = assertScreenFactorySuccess(
        screenParams = TestSources.testScreenParams,
        screen = TestSources.testAliasScreenWithCustomContext,
        factory = CUSTOM_CONTEXT_SCREEN_FACTORY,
    )

    private fun assertScreenFactorySuccess(
        screenParams: SourceFile,
        screen: SourceFile,
        factory: String,
    ) {
        val compilationResult = prepareCompilation(screenParams, screen)
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.OK)
        val screenFile = compilationResult.kspSourceFileDirectory.listFiles().find { it.name == "TestScreenFactory.kt" }
        assertNotNull(screenFile)
        assertEquals(factory, screenFile.readText())
    }

    /**
     * Компилирует переданные файлы, применяя [FactoryGeneratorSymbolProcessorProvider].
     * Не проверяет результат компиляции.
     */
    private fun prepareCompilation(
        vararg sourceFiles: SourceFile,
    ): JvmCompilationResult {
        return KotlinCompilation().apply {
            useKsp2()
            sources += sourceFiles
            symbolProcessorProviders += FactoryGeneratorSymbolProcessorProvider()
            inheritClassPath = true
        }.compile()
    }

    companion object {
        private val NO_ADDITIONAL_ARGS_SCREEN_FACTORY = """
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory() : ScreenFactory<ComponentContext, TestScreenParams, NoIntent, TestScreen> {
  override fun create(
    context: ComponentContext,
    params: TestScreenParams,
    intents: ReceiveChannel<NoIntent>,
  ): TestScreen = TestScreen(context, )
}

        """.trimIndent()

        private val CUSTOM_CONTEXT_SCREEN_FACTORY = """
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory() : ScreenFactory<TestComponentContext, TestScreenParams, NoIntent, TestScreen> {
  override fun create(
    context: TestComponentContext,
    params: TestScreenParams,
    intents: ReceiveChannel<NoIntent>,
  ): TestScreen = TestScreen(context, )
}

        """.trimIndent()

        private val SCREEN_PARAMS_ARGS_SCREEN_FACTORY = """
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory() : ScreenFactory<ComponentContext, TestScreenParams, NoIntent, TestScreen> {
  override fun create(
    context: ComponentContext,
    params: TestScreenParams,
    intents: ReceiveChannel<NoIntent>,
  ): TestScreen = TestScreen(params, context, )
}

        """.trimIndent()

        private val SCREEN_INTENTS_ARGS_SCREEN_FACTORY = """
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory() : ScreenFactory<ComponentContext, TestScreenParams, TestScreenIntent, TestScreen> {
  override fun create(
    context: ComponentContext,
    params: TestScreenParams,
    intents: ReceiveChannel<TestScreenIntent>,
  ): TestScreen = TestScreen(intents, context, )
}

        """.trimIndent()

        private val SCREEN_PARAMS_AND_INTENTS_ARGS_SCREEN_FACTORY = """
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory() : ScreenFactory<ComponentContext, TestScreenParams, TestScreenIntent, TestScreen> {
  override fun create(
    context: ComponentContext,
    params: TestScreenParams,
    intents: ReceiveChannel<TestScreenIntent>,
  ): TestScreen = TestScreen(params, intents, context, )
}

        """.trimIndent()

        private val ALL_ARGS_SCREEN_FACTORY = """
import com.arkivanov.decompose.ComponentContext
import kotlin.String
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

internal class TestScreenFactory(
  private val extra: String,
) : ScreenFactory<ComponentContext, TestScreenParams, TestScreenIntent, TestScreen> {
  override fun create(
    context: ComponentContext,
    params: TestScreenParams,
    intents: ReceiveChannel<TestScreenIntent>,
  ): TestScreen = TestScreen(extra, params, intents, context, )
}

        """.trimIndent()
    }
}
