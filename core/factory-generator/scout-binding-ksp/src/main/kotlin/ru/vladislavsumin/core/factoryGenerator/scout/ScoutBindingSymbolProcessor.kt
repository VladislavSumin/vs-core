package ru.vladislavsumin.core.factoryGenerator.scout

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent
import ru.vladislavsumin.core.factoryGenerator.GeneratedFactory
import ru.vladislavsumin.core.ksp.utils.Types
import ru.vladislavsumin.core.ksp.utils.processAnnotated
import ru.vladislavsumin.core.ksp.utils.writeTo

internal class ScoutBindingSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> =
        resolver.processAnnotated<GeneratedFactory> { processGeneratedFactoryAnnotation(it) }

    private fun processGeneratedFactoryAnnotation(instance: KSAnnotated) {
        // Проверяем тип объекта к которому применена аннотация
        if (instance !is KSClassDeclaration) {
            logger.error(
                message = "Is not a class. @GeneratedFactory applicable only to classes",
                symbol = instance,
            )
            return
        }
        generateScoutBinding(instance)
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun generateScoutBinding(
        instance: KSClassDeclaration,
    ) {
        // Ищем интерфейс от которого наследуется фабрика или, если его нет то берем саму фабрику
        val interfaceType = instance.superTypes.singleOrNull()
            ?.resolve()
            ?.toClassName()
            ?.takeIf { it != Types.Kotlin.Any }
            ?: instance.toClassName()

        val functionName = "register${interfaceType.simpleName}"

        val code = CodeBlock.builder()
            .addStatement("singleton<%T> {", interfaceType)
            .withIndent {
                addStatement("%T(", instance.toClassName())

                withIndent {
                    instance.primaryConstructor!!.parameters.forEach { parameter ->
                        val typeName = parameter.type.resolve().toTypeName()
                        val isLazy = (typeName as? ParameterizedTypeName)?.rawType == Types.Kotlin.Lazy
                        val innerType = if (isLazy) typeName.typeArguments.single() else typeName
                        val innerTypeRaw = when (innerType) {
                            is ParameterizedTypeName -> innerType.rawType
                            is ClassName -> innerType
                            else -> {
                                logger.error("Unexpected type $innerType", parameter)
                                return
                            }
                        }
                        val isNullable = innerType.isNullable

                        val getter = when {
                            isLazy && innerTypeRaw == Types.Kotlin.List && !isNullable -> "collectLazy()"
                            !isLazy && innerTypeRaw == Types.Kotlin.List && !isNullable -> "collect()"
                            innerTypeRaw == Types.Kotlin.List -> {
                                logger.error("Nullable list not allowed", parameter)
                                return
                            }

                            isLazy && innerTypeRaw == Types.Kotlin.Map && !isNullable -> "associateLazy()"
                            !isLazy && innerTypeRaw == Types.Kotlin.Map && !isNullable -> "associate()"
                            innerTypeRaw == Types.Kotlin.Map -> {
                                logger.error("Nullable map not allowed", parameter)
                                return
                            }

                            isLazy && !isNullable -> "getLazy()"
                            isLazy && isNullable -> "optLazy()"

                            isNullable -> "opt()"
                            !isNullable -> "get()"
                            else -> {
                                logger.error("Unexpected parameter", parameter)
                                return
                            }
                        }

                        addStatement("${parameter.name!!.asString()} = $getter,")
                    }
                }

                addStatement(")")
            }
            .addStatement("}")
            .build()

        FunSpec.builder(name = functionName)
            .addModifiers(KModifier.INTERNAL)
            .receiver(REGISTRY)
            .addCode(code)
            .addOriginatingKSFile(instance.containingFile!!)
            .build()
            .writeTo(codeGenerator, instance.packageName.asString(), instance.simpleName.asString() + "Registrar")
    }

    companion object {
        private val REGISTRY = ClassName("scout.definition", "Registry")
    }
}
