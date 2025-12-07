package ru.vladislavsumin.core.fs

import kotlinx.io.files.Path

internal class FileSystemServiceImpl(
    private val fileSystemBaseDirProvider: FileSystemBaseDirProvider,
) : FileSystemService {
    override fun getPreferencesDir(): Path =
        Path(fileSystemBaseDirProvider.getAppFileDir().toString() + "/data/preferences")
}
