package ru.vladislavsumin.core.factoryGenerator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import ru.vladislavsumin.core.ksp.utils.Types
import ru.vladislavsumin.core.ksp.utils.primaryConstructorWithPrivateFields
import ru.vladislavsumin.core.ksp.utils.processAnnotated
import ru.vladislavsumin.core.ksp.utils.writeTo

internal class FactoryGeneratorSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> =
        resolver.processAnnotated<GenerateFactory> { processGenerateFactoryAnnotation(resolver, it) }

    private fun processGenerateFactoryAnnotation(resolver: Resolver, instance: KSAnnotated) {
        // Проверяем тип объекта к которому применена аннотация
        if (instance !is KSClassDeclaration) {
            logger.error(
                message = "Is not a class. @GenerateFactory applicable only to classes",
                symbol = instance,
            )
            return
        }
        generateFactory(resolver, instance)
    }

    /**
     * Создает фабрику для создания экземпляров [instance] с одним методом create.
     *
     * @param instance инстанс который должна создавать фабрика
     */
    @OptIn(KspExperimental::class)
    @Suppress("LongMethod", "CyclomaticComplexMethod") // TODO отрефакторить + написать тесты.
    private fun generateFactory(
        resolver: Resolver,
        instance: KSClassDeclaration,
    ) {
        // TODO разобраться с получением инстанса аннотации для упрощения дальнейшей работы с ней
        val annotation = instance.annotations.first {
            it.annotationType.resolve().toClassName().canonicalName == GenerateFactory::class.qualifiedName
        }

        // Я без понятия почему, но аргументы могут быть пустыми несмотря на default value. А могут и нет...
        val factoryInterface =
            (annotation.arguments.find { it.name?.asString() == "factoryInterface" }?.value as? KSType)
                ?.toClassName()
                .let {
                    if (it == Types.Kotlin.Any) {
                        null
                    } else {
                        it
                    }
                }

        val visibilityModifier =
            (annotation.arguments.find { it.name?.asString() == "visibility" }?.value as? KSClassDeclaration)
                ?.simpleName
                ?.asString()
                ?.let { visibility ->
                    PackageVisibility.valueOf(visibility)
                } ?: PackageVisibility.Internal

        // Имя будущей фабрики.
        val name = (factoryInterface?.simpleName?.plus("Impl") ?: instance.simpleName.getShortName().plus("Factory"))

        val primaryConstructor = instance.primaryConstructor ?: let {
            logger.error(
                message = "For generate factory class must have primary constructor",
                symbol = instance,
            )
            return
        }

        // Список параметров в основном конструкторе.
        val constructorParams = primaryConstructor.parameters

        val factoryConstructorParams = if (factoryInterface == null) {
            constructorParams
                .filter { constructorParam ->
                    constructorParam.annotations
                        .map { it.toAnnotationSpec().typeName }
                        .none { it == BY_CREATE_ANNOTATION }
                }
        } else {
            val functionParams = resolver.getKotlinClassByName(factoryInterface.canonicalName)!!
                .getAllFunctions().first { it.simpleName.asString() == "create" }
                .parameters
                .map { it.name }
                .toSet()
            constructorParams.filter { it.name !in functionParams }
        }

        val functionParams = constructorParams - factoryConstructorParams

        // Блок кода который будет помещен в тело функции crate
        // return InstanceName(param1, param2)
        val returnCodeBlock = CodeBlock.builder()
            .add("return %T(", instance.toClassName())
            .apply {
                constructorParams.forEach { parameter ->
                    add("%L, ", parameter.name!!.getShortName())
                }
            }
            .add(")")
            .build()

        // Декларация функции create
        val createFunction = FunSpec.builder("create")
            .apply {
                functionParams.forEach {
                    addParameter(it.name!!.getShortName(), it.type.toTypeName())
                }
                if (factoryInterface != null) {
                    addModifiers(KModifier.OVERRIDE)
                }
            }
            .addCode(returnCodeBlock)
            .returns(instance.toClassName())
            .build()

        TypeSpec.classBuilder(name)
            .primaryConstructorWithPrivateFields(
                factoryConstructorParams.map { it.name!!.getShortName() to it.type.toTypeName() },
            )
            .apply {
                if (factoryInterface != null) {
                    addSuperinterface(factoryInterface)
                }
            }
            .addModifiers(visibilityModifier.toModifier())
            .addFunction(createFunction)
            .addOriginatingKSFile(instance.containingFile!!)
            .build()
            .writeTo(codeGenerator, instance.packageName.asString())
    }

    companion object {
        private val BY_CREATE_ANNOTATION = ClassName("ru.vladislavsumin.core.factoryGenerator", "ByCreate")
    }
}

private fun PackageVisibility.toModifier(): KModifier = when (this) {
    PackageVisibility.Public -> KModifier.PUBLIC
    PackageVisibility.Internal -> KModifier.INTERNAL
}
