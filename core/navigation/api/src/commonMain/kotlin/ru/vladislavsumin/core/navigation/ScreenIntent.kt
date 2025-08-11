package ru.vladislavsumin.core.navigation

/**
 * Общий маркер для событий передаваемых экрану по средствам навигации.
 */
public interface ScreenIntent

/**
 * Маркер для обозначения экранов которые не поддерживают [ScreenIntent]
 */
public data object NoIntent : ScreenIntent
