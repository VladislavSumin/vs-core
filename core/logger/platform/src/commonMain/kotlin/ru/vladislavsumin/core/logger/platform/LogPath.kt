package ru.vladislavsumin.core.logger.platform

public sealed class LogPath {
    /** Текущая рабочая директория + "logs". */
    public data object WorkDir : LogPath()

    /**
     * Домашняя папка пользователя + [appName] + "logs".
     * @param appName имя приложения для вложенной папки.
     */
    public data class UserHome(val appName: String) : LogPath()

    /**
     * Произвольный путь к директории.
     * @param path путь к папке с логами.
     */
    public data class Custom(val path: String) : LogPath()
}
