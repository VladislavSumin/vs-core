package ru.vladislavsumin.core.navigation.factoryGenerator

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toClassNameOrNull
import com.squareup.kotlinpoet.ksp.toTypeName
import ru.vladislavsumin.core.ksp.utils.Types
import ru.vladislavsumin.core.ksp.utils.primaryConstructorWithPrivateFields
import ru.vladislavsumin.core.ksp.utils.processAnnotated
import ru.vladislavsumin.core.ksp.utils.writeTo

internal class FactoryGeneratorSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> = resolver.processAnnotated<GenerateScreenFactory> {
        processGenerateFactoryAnnotation(it, resolver)
    }

    private fun processGenerateFactoryAnnotation(instance: KSAnnotated, resolver: Resolver) {
        check(instance is KSClassDeclaration) { "Only KSClassDeclaration supported, but $instance was received" }
        val primaryConstructor = instance.primaryConstructor
        checkNotNull(primaryConstructor) { "For generate factory class must have primary constructor" }
        generateFactory(instance, resolver)
    }

    /**
     * Создает фабрику для создания экземпляров [instance] с одним методом create.
     *
     * @param instance инстанс который должна создавать фабрика
     */
    private fun generateFactory(
        instance: KSClassDeclaration,
        resolver: Resolver,
    ) {
        // Имя будущей фабрики.
        val name = instance.simpleName.getShortName() + "Factory"

        // Список параметров в основном конструкторе.
        val constructorParams = instance.primaryConstructor!!.parameters

        // Ищем среди аргументов конструктора что-либо наследующееся от SCREEN_PARAMS_CLASS
        val screenParamsClassName: ClassName = constructorParams
            .find { param ->
                (param.type.resolve().declaration as? KSClassDeclaration)
                    ?.getAllSuperTypes()
                    ?.any { it.toClassNameOrNull() == SCREEN_PARAMS_CLASS } == true
            }
            ?.type?.toTypeName() as ClassName?
            ?: ClassName(
                packageName = instance.packageName.asString(),
                "${instance.simpleName.asString()}Params",
            )

        val screenIntentType = resolveScreenIntentType(screenParamsClassName, resolver)
        val screenIntentReceiveChannelType = Types.Coroutines.ReceiveChannel.parameterizedBy(screenIntentType)

        // Список параметров для конструктора фабрики
        val factoryConstructorParams = constructorParams
            .filter { it.type.toTypeName() != SCREEN_CONTEXT_CLASS }
            .filter { param -> param.type.toTypeName() != screenIntentReceiveChannelType }
            .filter { param ->
                (param.type.resolve().declaration as? KSClassDeclaration)
                    ?.getAllSuperTypes()
                    ?.any { it.toClassNameOrNull() == SCREEN_PARAMS_CLASS } != true
            }

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
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec.builder("context", SCREEN_CONTEXT_CLASS).build())
            .addParameter(ParameterSpec.builder("params", screenParamsClassName).build())
            .addParameter(ParameterSpec.builder("intents", screenIntentReceiveChannelType).build())
            .addCode(returnCodeBlock)
            .returns(instance.toClassName())
            .build()

        TypeSpec.classBuilder(name)
            .addSuperinterface(
                SCREEN_FACTORY_CLASS
                    .parameterizedBy(
                        screenParamsClassName,
                        screenIntentType,
                        instance.toClassName(),
                    ),
            )
            .primaryConstructorWithPrivateFields(
                factoryConstructorParams.map { it.name!!.getShortName() to it.type.toTypeName() },
            )
            .addModifiers(KModifier.INTERNAL)
            .addFunction(createFunction)
            .build()
            .writeTo(codeGenerator, instance.packageName.asString())
    }

    // TODO обработать тут все !! нормально
    private fun resolveScreenIntentType(
        screenParamsClassName: ClassName,
        resolver: Resolver,
    ): ClassName {
        val screenParamsClassDeclaration = resolver.getClassDeclarationByName(screenParamsClassName.canonicalName)!!
        val intentType = screenParamsClassDeclaration.getAllSuperTypes().find {
            (it.toTypeName() as? ParameterizedTypeName)?.rawType == SCREEN_PARAMS_CLASS
        }!!.arguments.first().toTypeName() as ClassName
        return intentType
    }

    companion object {
        private val SCREEN_CONTEXT_CLASS = ClassName("ru.vladislavsumin.core.navigation.screen", "ScreenContext")
        private val SCREEN_FACTORY_CLASS = ClassName("ru.vladislavsumin.core.navigation.screen", "ScreenFactory")
        private val SCREEN_PARAMS_CLASS = ClassName("ru.vladislavsumin.core.navigation", "IntentScreenParams")
    }
}
