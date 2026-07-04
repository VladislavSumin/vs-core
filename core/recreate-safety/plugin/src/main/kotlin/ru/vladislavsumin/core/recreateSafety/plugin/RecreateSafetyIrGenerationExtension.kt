@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package ru.vladislavsumin.core.recreateSafety.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

// ================================================================================================
// RecreateSafetyPluginRegistrar — точка входа плагина
// ================================================================================================

@OptIn(ExperimentalCompilerApi::class)
class RecreateSafetyPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = "ru.vladislavsumin.core.recreate-safety"
    override val supportsK2: Boolean get() = true
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(RecreateSafetyIrGenerationExtension())
    }
}

// ================================================================================================
// RecreateSafetyIrGenerationExtension
// ================================================================================================

class RecreateSafetyIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val gc = pluginContext.referenceClass(
            ClassId.topLevel(FqName("ru.vladislavsumin.core.decompose.components.GenericComponent"))
        ) ?: return
        val gs = pluginContext.referenceClass(
            ClassId.topLevel(FqName("ru.vladislavsumin.core.navigation.screen.GenericScreen"))
        ) ?: return
        pluginContext.referenceClass(
            ClassId.topLevel(FqName("ru.vladislavsumin.core.recreateSafety.RecreateSafe"))
        ) ?: return

        moduleFragment.acceptVoid(ViewModelSafetyVisitor(
            gc.owner.fqNameForIrSerialization!!.asString(),
            gs.owner.fqNameForIrSerialization!!.asString(),
            FqName("ru.vladislavsumin.core.recreateSafety.RecreateSafe"),
        ))
    }
}

// ================================================================================================
// ViewModelSafetyVisitor
// ================================================================================================

/**
 * Visitor IR-дерева. Находит вызовы viewModel(), извлекает лямбду-аргумент,
 * анализирует захваченные значения и проверяет их на @RecreateSafe.
 *
 * ## Поиск лямбды в K2 IR
 *
 * Вызов `viewModel(factory)` в K2 IR:
 *   call.arguments[0] = IrGetValue       (dispatch receiver)
 *   call.arguments[1] = IrFunctionExpression  (лямбда)
 *
 * Лямбда хранится как поле в анонимном классе:
 *   IrFunctionExpression.function.parent = IrField  (поле)
 *   IrField.parent = IrClass  (анонимный класс лямбды)
 *
 * Захваченные значения — параметры конструктора анонимного класса:
 *   IrClass → IrConstructor.parameters = [захват1, захват2, ...]
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
private class ViewModelSafetyVisitor(
    private val genericComponentFqn: String,
    private val genericScreenFqn: String,
    private val recreateSafeFqn: FqName,
) : IrVisitorVoid() {

    private val viewModelOwnerFqns = setOf(genericComponentFqn, genericScreenFqn)

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitCall(call: IrCall) {
        call.acceptChildrenVoid(this)

        // 1. Это вызов viewModel()?
        val function: IrSimpleFunction = call.symbol.owner
        if (function.name.asString() != "viewModel") return
        val ownerClass: IrClass = function.parentAsClass
        if (!ownerClass.isOrExtends(viewModelOwnerFqns)) return

        // 2. Ищем лямбду среди ВСЕХ аргументов
        val lambda: IrFunctionExpression = call.arguments
            .filterIsInstance<IrFunctionExpression>()
            .firstOrNull() ?: return

        // 3. Извлекаем захваты через цепочку IrFunctionExpression → IrField → IrClass
        val parentField = lambda.function.parent
        val anonClass = (parentField as IrDeclarationBase).parent as? IrClass ?: return

        val primaryCtor = anonClass.declarations
            .filterIsInstance<IrConstructor>()
            .firstOrNull { it.isPrimary }

        val captured = primaryCtor?.parameters?.map { param ->
            CapturedValue(name = param.name.asString(), type = param.type)
        } ?: emptyList()

        if (captured.isEmpty()) return

        // 4. Проверяем каждое захваченное значение
        for (cv in captured) {
            if (!isRecreateSafe(cv.type)) {
                reportError(cv)
            }
        }
    }

    // ===== Проверка иерархии =====

    private fun IrClass.isOrExtends(targetFqns: Set<String>): Boolean {
        val ownFqn = fqNameForIrSerialization?.asString()
        if (ownFqn != null && ownFqn in targetFqns) return true
        for (superType in superTypes) {
            val superClass = (superType as? IrSimpleType)?.classifier?.owner as? IrClass ?: continue
            if (superClass.isOrExtends(targetFqns)) return true
        }
        return false
    }

    // ===== Проверка @RecreateSafe =====

    private fun isRecreateSafe(type: org.jetbrains.kotlin.ir.types.IrType): Boolean {
        if (type !is IrSimpleType) return false
        val irClass = (type.classifier.owner as? IrClass) ?: return false
        return irClass.annotations.any { ann ->
            ann.symbol.owner.parentAsClass.fqNameForIrSerialization == recreateSafeFqn
        }
    }

    // ===== Ошибка =====

    private fun reportError(captured: CapturedValue) {
        val typeName = (captured.type as? IrSimpleType)?.classFqName?.asString()
            ?: captured.type.toString()
        throw IllegalStateException(buildString {
            appendLine("╔══════════════════════════════════════════════════════════╗")
            appendLine("║  ОШИБКА RecreateSafety: небезопасный захват во viewModel {} ║")
            appendLine("╚══════════════════════════════════════════════════════════╝")
            appendLine()
            appendLine("Значение '${captured.name}' типа '$typeName' захвачено в viewModel {},")
            appendLine("но тип НЕ помечен @RecreateSafe.")
            appendLine()
            appendLine("При пересоздании Screen/Component (смена конфигурации Android):")
            appendLine("  • ViewModel сохраняется через InstanceKeeper")
            appendLine("  • Screen/Component пересоздаётся заново")
            appendLine("  • viewModel {} НЕ вызывается повторно")
            appendLine("  => ViewModel навсегда сохранит СТАРУЮ ссылку на '${captured.name}'")
            appendLine()
            appendLine("Исправьте:")
            appendLine("  1. @RecreateSafe на тип '$typeName'")
            appendLine("  2. ИЛИ вынесите значение за пределы viewModel {} лямбды")
            appendLine("  3. ИЛИ оберните в StateFlow/Flow — передавайте флоу вместо значения")
        })
    }
}

// ================================================================================================
// CapturedValue
// ================================================================================================

private data class CapturedValue(
    val name: String,
    val type: org.jetbrains.kotlin.ir.types.IrType,
)
