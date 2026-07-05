package ru.vladislavsumin.core.navigation.transfer

import androidx.compose.runtime.saveable.SaveableStateRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SaveableStateRegistryImplTest {

    private fun registry(restored: Map<String, List<Any?>> = emptyMap()) =
        SaveableStateRegistryImpl(restored)

    private fun emptyPlatform() = SaveableStateRegistry(emptyMap()) { true }

    @Test
    fun `captureRaw returns registered provider values`() {
        val r = registry()
        r.registerProvider("a") { "hello" }
        r.registerProvider("b") { 42 }
        r.registerProvider("c") { listOf(1, 2, 3) }

        val captured = r.captureRaw()
        assertEquals(listOf("hello"), captured["a"])
        assertEquals(listOf(42), captured["b"])
        assertEquals(listOf(listOf(1, 2, 3)), captured["c"])
    }

    @Test
    fun `consumeRestored returns values from constructor`() {
        val restored = mapOf(
            "x" to listOf("world"),
            "y" to listOf(99, 100),
        )
        val r = registry(restored)

        assertEquals("world", r.consumeRestored("x"))
        assertEquals(99, r.consumeRestored("y")) // first of multi-value
        assertEquals(100, r.consumeRestored("y")) // second
        assertNull(r.consumeRestored("y")) // exhausted
        assertNull(r.consumeRestored("missing"))
    }

    @Test
    fun `consumeRestored falls back to platform`() {
        val platform = SaveableStateRegistry(mapOf("pf" to listOf("pf_value"))) { true }
        val r = SaveableStateRegistryImpl(emptyMap(), platform)

        assertEquals("pf_value", r.consumeRestored("pf"))
        assertNull(r.consumeRestored("pf")) // consumed from platform
    }

    @Test
    fun `consumeRestored prefers raw over platform`() {
        val platform = SaveableStateRegistry(mapOf("k" to listOf("pf"))) { true }
        val r = SaveableStateRegistryImpl(mapOf("k" to listOf("raw")), platform)

        assertEquals("raw", r.consumeRestored("k")) // raw wins
        assertEquals("pf", r.consumeRestored("k")) // raw exhausted, falls back to platform
        assertNull(r.consumeRestored("k")) // both exhausted
    }

    @Test
    fun `performSave delegates to platform`() {
        val platform = emptyPlatform()
        platform.registerProvider("pf") { "pf_val" }
        val r = SaveableStateRegistryImpl(emptyMap(), platform)

        val result = r.performSave()
        assertEquals("pf_val", result["pf"]?.single())
    }

    @Test
    fun `attachPlatform re-registers providers in new platform`() {
        val r = registry()
        r.registerProvider("k") { "old" }

        val newPlatform = emptyPlatform()
        r.attachPlatform(newPlatform)

        // Provider is now registered in new platform
        val platformResult = newPlatform.performSave()
        assertEquals("old", platformResult["k"]?.single())
    }

    @Test
    fun `attachPlatform removes providers from old platform`() {
        val oldPlatform = emptyPlatform()
        val r = SaveableStateRegistryImpl(emptyMap(), oldPlatform)
        r.registerProvider("k") { "v" }

        val newPlatform = emptyPlatform()
        r.attachPlatform(newPlatform)

        val oldResult = oldPlatform.performSave()
        assertNull(oldResult["k"]) // removed from old platform
    }

    @Test
    fun `canBeSaved delegates to platform`() {
        val platformTrue = SaveableStateRegistry(emptyMap()) { true }
        val platformFalse = SaveableStateRegistry(emptyMap()) { false }
        val rNoPlatform = registry()
        val rTrue = SaveableStateRegistryImpl(emptyMap(), platformTrue)
        val rFalse = SaveableStateRegistryImpl(emptyMap(), platformFalse)

        assertTrue(rNoPlatform.canBeSaved("anything"))
        assertTrue(rTrue.canBeSaved("anything"))
        assertFalse(rFalse.canBeSaved("anything"))
    }

    @Test
    fun `entry unregister removes provider`() {
        val r = registry()
        val entry = r.registerProvider("k") { "v" }
        assertNotNull(r.captureRaw()["k"])

        entry.unregister()
        assertNull(r.captureRaw()["k"])
    }

    @Test
    fun `entry unregister also removes from platform`() {
        val platform = emptyPlatform()
        val r = SaveableStateRegistryImpl(emptyMap(), platform)
        val entry = r.registerProvider("k") { "v" }

        entry.unregister()
        assertNull(platform.performSave()["k"])
    }
}
