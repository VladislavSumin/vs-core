package ru.vladislavsumin.core.fs

import android.content.Context
import kotlinx.io.files.Path
import org.kodein.di.DirectDI
import ru.vladislavsumin.core.di.i

private class FileSystemBaseDirProviderImpl(private val context: Context) : FileSystemBaseDirProvider {
    override fun getAppFileDir(): Path = Path(context.filesDir.absolutePath)
}

internal actual fun DirectDI.createFileSystemBaseDirProvider(): FileSystemBaseDirProvider {
    return FileSystemBaseDirProviderImpl(i())
}
