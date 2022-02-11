package ru.vs.build_script.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction


abstract class CopyFromResourceTask : DefaultTask() {
    @get:Input
    abstract val from: Property<String>

    @get:OutputFile
    abstract val to: RegularFileProperty

    @TaskAction
    fun action() {
        val data = javaClass.classLoader.getResourceAsStream(from.get())!!.readAllBytes()
        to.get().asFile.writeBytes(data)
    }
}