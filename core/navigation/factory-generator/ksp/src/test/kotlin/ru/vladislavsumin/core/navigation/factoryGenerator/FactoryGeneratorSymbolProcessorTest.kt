package ru.vladislavsumin.core.navigation.factoryGenerator

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import ru.vladislavsumin.core.ksp.test.kspSourceFileDirectory
import ru.vladislavsumin.core.ksp.test.prepareCompilation
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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

    @Test
    fun testScreenNotImplementingScreen() {
        val result = prepareCompilation(
            FactoryGeneratorSymbolProcessorProvider(),
            TestSources.testScreenNotImplementingScreen,
        )
        assertNotEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertContains(
            result.messages,
            "NotAScreen.kt:4: @GenerateScreenFactory only applicable to classes implementing Screen",
        )
    }

    @Test
    fun testScreenWithoutPrimaryConstructor() {
        val result = prepareCompilation(
            FactoryGeneratorSymbolProcessorProvider(),
            TestSources.testScreenWithoutPrimaryConstructor,
        )
        assertNotEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertContains(
            result.messages,
            "NoPrimaryScreen.kt:8: To generate screen factory screen class must have primary constructor",
        )
    }

    @Test
    fun testScreenWithMultipleScreenParams() {
        val result = prepareCompilation(
            FactoryGeneratorSymbolProcessorProvider(),
            TestSources.testScreenWithMultipleScreenParams,
        )
        assertNotEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertContains(result.messages, "MultiParamsScreen.kt:16: Screen contains more than once screen params")
    }

    @Test
    fun testScreenParamsWithWrongName() {
        val result = prepareCompilation(
            FactoryGeneratorSymbolProcessorProvider(),
            TestSources.testScreenParamsWithIntent,
            TestSources.testScreenParamsWithWrongName,
        )
        assertNotEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertContains(result.messages, "WrongNameScreen.kt:9: Screen params variable must be named \"params\"")
    }

    @Test
    fun testScreenParamsAutoResolvedNotFound() {
        val result = prepareCompilation(
            FactoryGeneratorSymbolProcessorProvider(),
            TestSources.testScreenParamsAutoResolvedNotFound,
        )
        assertNotEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertContains(
            result.messages,
            "OrphanScreen.kt:8: ScreenParams not found and automatically resolved as OrphanScreenParams, " +
                "but screenParams with current type not exist",
        )
    }

    private fun assertScreenFactorySuccess(
        screenParams: SourceFile,
        screen: SourceFile,
        factory: String,
    ) {
        val compilationResult = prepareCompilation(FactoryGeneratorSymbolProcessorProvider(), screenParams, screen)
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.OK)
        val screenFile = compilationResult.kspSourceFileDirectory.listFiles().find { it.name == "TestScreenFactory.kt" }
        assertNotNull(screenFile)
        assertEquals(factory, screenFile.readText())
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
