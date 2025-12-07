package ru.vladislavsumin.core.fs

import kotlinx.io.files.Path
import org.kodein.di.DirectDI
import ru.vladislavsumin.core.di.i
import java.io.File

private class FileSystemBaseDirProviderImpl(
    private val fsApplicationMeta: FsApplicationMeta,
) : FileSystemBaseDirProvider {
    private val homeDir by lazy {
        val homePath = System.getProperty("user.home")
        File(homePath)
    }

    private val appDir by lazy { homeDir.resolve(fsApplicationMeta.name) }

    override fun getAppFileDir(): Path = Path(appDir.absolutePath)
}

internal actual fun DirectDI.createFileSystemBaseDirProvider(): FileSystemBaseDirProvider {
    return FileSystemBaseDirProviderImpl(i())
}
