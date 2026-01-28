package ru.vladislavsumin.core.factoryGenerator

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

    val classWithNoParamsAndPublicModifier = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            import ru.vladislavsumin.core.factoryGenerator.PackageVisibility
            
            @GenerateFactory(visibility = PackageVisibility.Public)
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

    val classWithByCreate = SourceFile.kotlin(
        name = "TestClass.kt",
        contents = """
            import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
            import ru.vladislavsumin.core.factoryGenerator.ByCreate
            
            @GenerateFactory
            class TestClass(
                private val test: String, 
                @ByCreate private val byCreate: String,
            )
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
