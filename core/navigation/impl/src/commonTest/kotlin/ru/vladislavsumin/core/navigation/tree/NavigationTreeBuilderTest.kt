package ru.vladislavsumin.core.navigation.tree

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.collections.tree.asSequence
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.repository.NavigationRepositoryImpl
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.NavigationHostB
import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.testData.ScreenB
import ru.vladislavsumin.core.navigation.testData.ScreenC
import ru.vladislavsumin.core.navigation.utils.FailingScreenFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

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

    @Test
    fun testScreenNotRegisteredInTree() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenB::class)
                    },
                )
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        assertFailsWith<ScreenNotRegisteredException> {
            builder.build()
        }
    }

    @Test
    fun testTreeWithMultipleHosts() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenB::class)
                        NavigationHostB opens setOf(ScreenC::class)
                    },
                )
                registerScreen(factory = FailingScreenFactory<ScreenB>())
                registerScreen(factory = FailingScreenFactory<ScreenC>())
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        val tree = builder.build()
        assertEquals(3, tree.asSequence().count())
        assertEquals(2, tree.children.size)
    }

    @Test
    fun testTreeWithNestedScreens() {
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
                        NavigationHostA opens setOf(ScreenC::class)
                    },
                )
                registerScreen(factory = FailingScreenFactory<ScreenC>())
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val builder = NavigationTreeBuilder(repository)
        val tree = builder.build()
        assertEquals(3, tree.asSequence().count())
        assertEquals(ScreenB.asKey(), tree.children.single().value.screenKey)
        assertEquals(ScreenC.asKey(), tree.children.single().children.single().value.screenKey)
    }

    @Test
    fun testTreeScreenInfoContainsHostInParent() {
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
        assertNotNull(tree.children.single().value.hostInParent)
        assertEquals(NavigationHostA, tree.children.single().value.hostInParent)
    }
}
