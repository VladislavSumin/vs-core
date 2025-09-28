package ru.vladislavsumin.configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType

/**
 * Файл пропертей проекта, содержит dsl для доступа к любым проперти используемым при сборке.
 */
@Suppress("UnnecessaryAbstractClass")
open class ProjectConfiguration(project: Project, propertyProvider: PropertyProvider) :
    Configuration(project, "ru.vs", propertyProvider) {

    val version = property("version", "0.0.1")
    val basePackage = property<String>("basePackage")
    val core = CoreConfiguration()
    val signing = Signing()
    val sonatype = Sonatype()

    inner class CoreConfiguration : Configuration("core", this) {
        /**
         * Версия jvm используемая для сборки проекта
         */
        val jvmVersion = property("jvmVersion", "21")

        val android = Android()

        /**
         * Настройки android плагина.
         */
        @Suppress("MagicNumber") // В данном случае значение цифр понятно без пояснения.
        inner class Android : Configuration("android", this) {
            val minSdk = property("minSdk", 26)
            val targetSdk = property("targetSdk", 35)
            val compileSdk = property("compileSdk", 35)
        }
    }

    /**
     * Настройки подписи
     */
    inner class Signing : Configuration("signing", this) {
        val keyId = property("keyId", "")
        val password = property("password", "")
        val secretKeyRingFile = property("secretKeyRingFile", "")
    }

    /**
     * Настройки для sonatype репозитория
     */
    inner class Sonatype : Configuration("sonatype", this) {
        val username = property("username", "")
        val password = property("password", "")
    }
}

/**
 * Единожды создает инстанс [ProjectConfiguration] после чего возвращает его кешированное значение.
 */
val Project.projectConfiguration: ProjectConfiguration
    get() = rootProject.extensions.findByType()
        ?: rootProject.extensions.create(
            ProjectConfiguration::class.java.simpleName,
            project,
            propertyProvider,
        )

private val Project.propertyProvider
    get() = PropertyProvider {
        System.getenv(it) ?: project.findProperty(it)?.toString()
    }
