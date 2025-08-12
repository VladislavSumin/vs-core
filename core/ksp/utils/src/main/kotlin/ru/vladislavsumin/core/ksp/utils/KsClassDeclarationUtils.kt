package ru.vladislavsumin.core.ksp.utils

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toTypeName

public fun KSClassDeclaration.findParametrizedSuperTypeOrNull(className: ClassName): ParameterizedTypeName? {
    return getAllSuperTypes()
        .mapNotNull { type ->

            // Пробуем зарезолвить класс
            var parametrized: ParameterizedTypeName? = type.toTypeName() as? ParameterizedTypeName

            // Пробуем зарезолвить TypeAlias
            if (parametrized == null) {
                parametrized = ((type.declaration as? KSTypeAlias)?.type?.toTypeName() as? ParameterizedTypeName)
            }

            if (parametrized?.rawType == className) parametrized else null
        }
        .firstOrNull()
}
