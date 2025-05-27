package ru.vladislavsumin.core.navigation.tree

import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.screen.ScreenKey

public class ScreenNotRegisteredException internal constructor(
    parent: ScreenKey?,
    hostInParent: NavigationHost?,
    child: ScreenKey,
) : Exception("Child screen $child, requested by $hostInParent in screen $parent, but not registered")
