package ru.vladislavsumin.core.fs

import kotlinx.io.files.Path
import org.kodein.di.DirectDI

internal interface FileSystemBaseDirProvider {
    /**
     * Возвращает путь к папке предназначенной для хранения файлов приложения.
     */
    fun getAppFileDir(): Path
}

/**
 * Создает платформенную реализацию [FileSystemBaseDirProvider]
 */
internal expect fun DirectDI.createFileSystemBaseDirProvider(): FileSystemBaseDirProvider
