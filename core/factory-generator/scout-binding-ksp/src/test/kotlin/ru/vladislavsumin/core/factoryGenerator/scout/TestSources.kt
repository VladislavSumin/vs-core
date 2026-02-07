package ru.vladislavsumin.core.factoryGenerator.scout

import com.tschuchort.compiletesting.SourceFile

object TestSources {
    val classWithNoParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass
        """.trimIndent(),
    )

    val classWithParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: String)
        """.trimIndent(),
    )

    val classWithListParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: List<String>)
        """.trimIndent(),
    )

    val classWithLazyListParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: Lazy<List<String>>)
        """.trimIndent(),
    )

    val classWithMapParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: Map<String, String>)
        """.trimIndent(),
    )

    val classWithLazyMapParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: Lazy<Map<String, String>>)
        """.trimIndent(),
    )

    val classWithNullableParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: String?)
        """.trimIndent(),
    )

    val classWithLazyParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: Lazy<String>)
        """.trimIndent(),
    )

    val classWithLazyNullableParams = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            @GenerateFactory
            class TestClass(private val test: Lazy<String?>)
        """.trimIndent(),
    )

    val classWithInterface = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            
            interface TestClassFactory {
                fun create(byCreate: String): TestClass
            }
            
            @GenerateFactory(TestClassFactory::class)
            class TestClass(
                private val test: String, 
                private val byCreate: String,
            )
        """.trimIndent(),
    )
}
