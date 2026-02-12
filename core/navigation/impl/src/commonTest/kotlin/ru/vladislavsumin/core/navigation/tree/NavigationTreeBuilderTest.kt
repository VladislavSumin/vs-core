package ru.vladislavsumin.core.navigation.tree

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.collections.tree.asSequence
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.repository.NavigationRepositoryImpl
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.testData.ScreenB
import ru.vladislavsumin.core.navigation.utils.FailingScreenFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NavigationTreeBuilderTest {
    @Test
    fun testEmpty() {
        val repository = NavigationRepositoryImpl<ComponentContext>(emptySet())
        val builder = NavigationTreeBuilder(repository)
        assertFailsWith<NoRootFoundException> {
            builder.build()
        }
    }

    @Test
    fun testSingleScreen() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(factory = FailingScreenFactory<ScreenA>())
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        val tree = builder.build()
        assertEquals(1, tree.asSequence().count())
    }

    @Test
    fun testChainScreen() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenB::class)
                    },
                )
                registerScreen(factory = FailingScreenFactory<ScreenB>())
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        val tree = builder.build()
        assertEquals(2, tree.asSequence().count())
        assertEquals(ScreenA.asKey(), tree.value.screenKey)
        assertEquals(ScreenB.asKey(), tree.children.single().value.screenKey)
    }

    @Test
    fun testNoRoots() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenB::class)
                    },
                )
                registerScreen(
                    factory = FailingScreenFactory<ScreenB>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenA::class)
                    },
                )
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        assertFailsWith<NoRootFoundException> {
            builder.build()
        }
    }

    @Test
    fun testNoRootsBySelf() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenA::class)
                    },
                )
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        assertFailsWith<NoRootFoundException> {
            builder.build()
        }
    }

    @Test
    fun testMoreThanOneRoot() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(factory = FailingScreenFactory<ScreenA>())
                registerScreen(factory = FailingScreenFactory<ScreenB>())
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        assertFailsWith<MoreThanOneRootFoundException> {
            builder.build()
        }
    }
}
