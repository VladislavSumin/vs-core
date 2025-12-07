package ru.vladislavsumin.core.fs

import kotlinx.io.files.Path

public interface FileSystemService {
    /**
     * Возвращает путь к папке, предназначенной для хранения префов.
     */
    public fun getPreferencesDir(): Path
}
