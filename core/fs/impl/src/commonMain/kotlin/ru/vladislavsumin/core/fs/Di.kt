package ru.vladislavsumin.core.fs

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules

public fun Modules.coreFs(
    appTechName: String,
): DI.Module = DI.Module("core-fs") {
    bindSingleton<FsApplicationMeta> { FsApplicationMeta(name = appTechName) }

    bindSingleton<FileSystemService> {
        val baseFileSystemBaseDirProvider = createFileSystemBaseDirProvider()
        FileSystemServiceImpl(baseFileSystemBaseDirProvider)
    }
}
