package ru.vladislavsumin.core.factoryGenerator.scout

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

public class ScoutBindingSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ScoutBindingSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}
