package ru.vladislavsumin.core.factoryGenerator.scout

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import ru.vladislavsumin.core.factoryGenerator.FactoryGeneratorSymbolProcessorProvider
import ru.vladislavsumin.core.ksp.test.kspSourceFileDirectory
import ru.vladislavsumin.core.ksp.test.prepareCompilation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCompilerApi::class)
class ScoutBindingSymbolProcessorTest {
    @Test
    fun testFactoryWithoutParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithNoParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                    )
                  }
                }
                
            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = get(),
                    )
                  }
                }

            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithLazyParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithLazyParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = getLazy(),
                    )
                  }
                }

            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithInterface() {
        assertScreenFactorySuccess(
            source = TestSources.classWithInterface,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactoryImpl(
                      test = get(),
                    )
                  }
                }

            """.trimIndent(),
        )
    }

    private fun assertScreenFactorySuccess(
        source: SourceFile,
        factory: String,
    ) {
        val compilationResult = prepareCompilation(
            symbolProcessorProviders = listOf(
                FactoryGeneratorSymbolProcessorProvider(),
                ScoutBindingSymbolProcessorProvider(),
            ),
            source,
        )
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.OK)
        val binderFile = compilationResult.kspSourceFileDirectory.listFiles().single {
            it.name.endsWith("Registrar.kt")
        }
        assertNotNull(binderFile)
        assertEquals(factory, binderFile.readText())
    }
}
