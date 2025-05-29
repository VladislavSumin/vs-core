package ru.vladislavsumin.core.ksp.utils

import com.squareup.kotlinpoet.ClassName

/**
 * Набор стандартных типов для использования в кодогенерации.
 */
public object Types {
    public object Coroutines {
        public val Flow: ClassName = ClassName("kotlinx.coroutines.flow", "Flow")
        public val CoroutineScope: ClassName = ClassName("kotlinx.coroutines", "CoroutineScope")
        public val GlobalScope: ClassName = ClassName("kotlinx.coroutines", "GlobalScope")
        public val ReceiveChannel: ClassName = ClassName("kotlinx.coroutines.channels", "ReceiveChannel")
    }

    public object Serialization {
        public val ProtoBuf: ClassName = ClassName("kotlinx.serialization.protobuf", "ProtoBuf")
    }
}
