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
