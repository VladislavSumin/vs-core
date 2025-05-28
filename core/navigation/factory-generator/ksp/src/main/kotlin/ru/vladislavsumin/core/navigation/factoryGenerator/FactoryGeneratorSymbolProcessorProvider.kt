package ru.vladislavsumin.core.navigation.factoryGenerator

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

public class FactoryGeneratorSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FactoryGeneratorSymbolProcessor(environment.codeGenerator)
    }
}
