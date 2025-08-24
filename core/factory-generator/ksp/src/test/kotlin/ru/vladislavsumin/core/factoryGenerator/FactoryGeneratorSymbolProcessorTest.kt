package ru.vladislavsumin.core.factoryGenerator

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import ru.vladislavsumin.core.ksp.test.kspSourceFileDirectory
import ru.vladislavsumin.core.ksp.test.prepareCompilation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCompilerApi::class)
class FactoryGeneratorSymbolProcessorTest {
    @Test
    fun testFactoryWithoutParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithNoParams,
            factory = """
                internal class TestClassFactory() {
                  public fun create(): TestClass = TestClass()
                }
                
            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithParams,
            factory = """
                import kotlin.String

                internal class TestClassFactory(
                  private val test: String,
                ) {
                  public fun create(): TestClass = TestClass(test, )
                }
                
            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithByCreate() {
        assertScreenFactorySuccess(
            source = TestSources.classWithByCreate,
            factory = """
                import kotlin.String

                internal class TestClassFactory(
                  private val test: String,
                ) {
                  public fun create(byCreate: String): TestClass = TestClass(test, byCreate, )
                }
                
            """.trimIndent(),
        )
    }

    private fun assertScreenFactorySuccess(
        source: SourceFile,
        factory: String,
    ) {
        val compilationResult = prepareCompilation(FactoryGeneratorSymbolProcessorProvider(), source)
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.OK)
        val screenFile = compilationResult.kspSourceFileDirectory.listFiles().single()
        assertNotNull(screenFile)
        assertEquals(factory, screenFile.readText())
    }
}
