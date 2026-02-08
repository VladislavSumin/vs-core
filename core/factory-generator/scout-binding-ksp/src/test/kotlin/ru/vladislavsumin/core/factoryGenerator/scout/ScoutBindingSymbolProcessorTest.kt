package ru.vladislavsumin.core.factoryGenerator.scout

import com.tschuchort.compiletesting.DiagnosticSeverity
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
    fun testFactoryWithListParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithListParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = collect(),
                    )
                  }
                }

            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithLazyListParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithLazyListParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = collectLazy(),
                    )
                  }
                }

            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithMapParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithMapParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = associate(),
                    )
                  }
                }

            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithLazyMapParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithLazyMapParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = associateLazy(),
                    )
                  }
                }

            """.trimIndent(),
        )
    }

    @Test
    fun testFactoryWithNullableParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithNullableParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = opt(),
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
    fun testFactoryWithLazyNullableParams() {
        assertScreenFactorySuccess(
            source = TestSources.classWithLazyNullableParams,
            factory = """
                import scout.definition.Registry
                
                internal fun Registry.registerTestClassFactory() {
                  singleton<TestClassFactory> {
                    TestClassFactory(
                      test = optLazy(),
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

    @Test
    fun testFactoryWithNullableListParams() {
        assertScreenFactoryFail(
            source = TestSources.classWithNullableListParams,
            message = "TestClassFactory.kt:7: Nullable list not allowed",
        )
    }

    @Test
    fun testFactoryWithNullableMapParams() {
        assertScreenFactoryFail(
            source = TestSources.classWithNullableMapParams,
            message = "TestClassFactory.kt:7: Nullable map not allowed",
        )
    }

    private fun assertScreenFactoryFail(
        source: SourceFile,
        message: String,
    ) {
        val compilationResult = compile(source)
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertNotNull(
            actual = compilationResult.diagnosticMessages.single {
                it.severity == DiagnosticSeverity.ERROR && it.message.contains(message)
            },
        ) { "Output not contains '$message', output:\n${compilationResult.diagnosticMessages}" }
    }

    private fun assertScreenFactorySuccess(
        source: SourceFile,
        factory: String,
    ) {
        val compilationResult = compile(source)
        assertEquals(compilationResult.exitCode, KotlinCompilation.ExitCode.OK)
        val binderFile = compilationResult.kspSourceFileDirectory.listFiles().single {
            it.name.endsWith("Registrar.kt")
        }
        assertNotNull(binderFile)
        assertEquals(factory, binderFile.readText())
    }

    private fun compile(source: SourceFile) = prepareCompilation(
        symbolProcessorProviders = listOf(
            FactoryGeneratorSymbolProcessorProvider(),
            ScoutBindingSymbolProcessorProvider(),
        ),
        source,
    )
}
