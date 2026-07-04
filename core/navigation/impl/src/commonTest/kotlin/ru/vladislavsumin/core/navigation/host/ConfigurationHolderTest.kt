package ru.vladislavsumin.core.navigation.host

import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.TestLeafIntent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Характеризационные тесты [ConfigurationHolder]: равенство только по screenParams и доставка intent'ов
 * через внутренний канал.
 */
class ConfigurationHolderTest {

    @Test
    fun equalsAndHashCodeUseScreenParamsOnly() {
        val a = ConfigurationHolder(LeafParams(1))
        val b = ConfigurationHolder(LeafParams(1))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun differentScreenParamsAreNotEqual() {
        assertNotEquals(ConfigurationHolder(LeafParams(1)), ConfigurationHolder(LeafParams(2)))
    }

    @Test
    fun equalityIgnoresIntentMetadata() {
        // Разные intent'ы, но одинаковые screenParams -> holder'ы равны (intent — это метаданные).
        val a = ConfigurationHolder(LeafParams(1), TestLeafIntent(1))
        val b = ConfigurationHolder(LeafParams(1), TestLeafIntent(2))
        assertEquals(a, b)
    }

    @Test
    fun initialIntentIsDelivered() = runTest {
        val holder = ConfigurationHolder(LeafParams(1), TestLeafIntent(7))
        assertEquals(TestLeafIntent(7), holder.intentReceiveChannel.receive())
    }

    @Test
    fun nullInitialIntentIsNotDelivered() = runTest {
        val holder = ConfigurationHolder(LeafParams(1), initialIntent = null)
        holder.sendIntent(TestLeafIntent(3))
        // Первым в канале должен оказаться именно отправленный intent, а не null.
        assertEquals(TestLeafIntent(3), holder.intentReceiveChannel.receive())
    }

    @Test
    fun sendIntentDeliversInOrderAndSkipsNulls() = runTest {
        val holder = ConfigurationHolder(LeafParams(1))
        holder.sendIntent(TestLeafIntent(1))
        holder.sendIntent(null)
        holder.sendIntent(TestLeafIntent(2))

        assertEquals(TestLeafIntent(1), holder.intentReceiveChannel.receive())
        assertEquals(TestLeafIntent(2), holder.intentReceiveChannel.receive())
    }
}
