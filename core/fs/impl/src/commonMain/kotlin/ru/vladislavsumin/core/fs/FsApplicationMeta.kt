package ru.vladislavsumin.core.fs

/**
 * Мета информация для получения дополнительных данных о приложении.
 *
 * @param name - техническое название приложение, для linux like операционных систем это имя будет являться названием
 * папки приложения в домашней директории
 */
internal data class FsApplicationMeta(val name: String)
