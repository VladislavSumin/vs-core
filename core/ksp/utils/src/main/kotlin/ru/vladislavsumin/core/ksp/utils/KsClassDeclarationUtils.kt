package ru.vladislavsumin.core.ksp.utils

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Ищет среди всех супертипов [this] тип по классу соответствующий переданному [className] и возвращает его вместе
 * с его параметрами
 */
public fun KSClassDeclaration.findParametrizedSuperTypeOrNull(className: ClassName): ParameterizedTypeName? {
    return getAllSuperTypes()
        .mapNotNull { type ->

            // Пробуем зарезолвить класс
            var parametrized: ParameterizedTypeName? = type.toTypeNameOrNull() as? ParameterizedTypeName

            // Пробуем зарезолвить TypeAlias
            if (parametrized == null) {
                parametrized = ((type.declaration as? KSTypeAlias)?.type?.toTypeName() as? ParameterizedTypeName)
            }

            if (parametrized?.rawType == className) parametrized else null
        }
        .firstOrNull()
}
