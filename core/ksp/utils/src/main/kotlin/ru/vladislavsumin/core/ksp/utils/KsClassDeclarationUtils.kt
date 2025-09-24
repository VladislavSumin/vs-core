package ru.vladislavsumin.core.ksp.utils

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import java.util.NoSuchElementException
import kotlin.collections.forEachIndexed

/**
 * Ищет среди всех супертипов [this] тип по классу соответствующий переданному [className] и возвращает его вместе
 * с его параметрами
 */
// TODO написать комментарии на ЭТО
public fun KSClassDeclaration.findParametrizedSuperTypeOrNull(className: ClassName): ParameterizedTypeName? {
    var typeResolver: TypeParameterResolver = TypeParameterResolver.EMPTY
    return getAllSuperTypes()
        .mapNotNull { type ->

            // Пробуем зарезолвить класс
            var parametrized: ParameterizedTypeName? = type.toTypeNameOrNull(typeResolver) as? ParameterizedTypeName

            // Пробуем зарезолвить TypeAlias
            if (parametrized == null) {
                val alias = (type.declaration as? KSTypeAlias)
                if (alias != null) {
                    typeResolver = alias.typeParameters.toTypeParameterResolver(typeResolver)
                    parametrized = alias.type.toTypeName(typeResolver) as? ParameterizedTypeName

                    val keys = alias.type.resolve().declaration.typeParameters.map { it.name.asString() }
                    val values =
                        alias.type.resolve().arguments.map { it.type!!.resolve().toTypeName() }
                    val map = mutableMapOf<String, TypeVariableName>()
                    keys.forEachIndexed { index, string ->
                        map[string] = TypeVariableName(string, values[index])
                    }

                    typeResolver = object : TypeParameterResolver {
                        override fun get(index: String): TypeVariableName {
                            return parametersMap[index]
                                ?: throw NoSuchElementException("No TypeParameter found for index $index")
                        }

                        override val parametersMap: Map<String, TypeVariableName> = typeResolver.parametersMap + map
                    }
                }
            } else {
                val keys = type.declaration.typeParameters.map { it.name.asString() }
                val values =
                    type.arguments.map { it.type!!.resolve().toTypeNameOrNull() }
                val map = mutableMapOf<String, TypeVariableName>()
                keys.forEachIndexed { index, string ->
                    val value = values[index]
                    if (value != null) {
                        map[string] = TypeVariableName(string, value)
                    }
                }

                typeResolver = object : TypeParameterResolver {
                    override fun get(index: String): TypeVariableName {
                        return parametersMap[index]
                            ?: throw NoSuchElementException("No TypeParameter found for index $index")
                    }

                    override val parametersMap: Map<String, TypeVariableName> = typeResolver.parametersMap + map
                }
            }

            if (parametrized?.rawType == className) {
                val newArgs = parametrized.typeArguments.map {
                    when (it) {
                        is TypeVariableName -> it.bounds.first()
                        else -> it
                    }
                }
                parametrized.copy(typeArguments = newArgs)
            } else {
                null
            }
        }
        .firstOrNull()
}
