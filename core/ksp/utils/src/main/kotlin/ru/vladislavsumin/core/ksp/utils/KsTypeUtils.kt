package ru.vladislavsumin.core.ksp.utils

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

public fun KSType.toTypeNameOrNull(
    typeParamResolver: TypeParameterResolver = TypeParameterResolver.EMPTY,
): TypeName? =
    try {
        toTypeName(typeParamResolver)
    } catch (_: NoSuchElementException) {
        // Эта ошибка выскакивает для типов вида
        // interface SomeInterface<out T : Any>
        // TODO Посмотреть issues на эту тему, кажется это баг.
        null
    }
