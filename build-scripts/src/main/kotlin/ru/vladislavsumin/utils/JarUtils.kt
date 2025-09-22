package ru.vladislavsumin.utils

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/**
 * Creates tasks buildFarJar[flavor] and runJvm[flavor] for current [KotlinJvmTarget].
 * Jar file will be stored at `build/libs/[jarName].jar`.
 *
 * @param mainClass main class for store in manifest inside jar and for running with jvmRun[flavor].
 * @param flavor flavor name (default is main).
 * @param jarName name for jar archive.
 * @param duplicatesStrategy strategy for copy spec, sometime we may change it for some reason.
 */
fun KotlinJvmTarget.fatJar(
    mainClass: String,
    flavor: String = "main",
    jarName: String = "${project.name}-fat",
    duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
): TaskProvider<Jar> {
    val main = compilations.getByName(flavor)
    // See https://stackoverflow.com/questions/57168853/create-fat-jar-from-kotlin-multiplatform-project
    return project.tasks.register<Jar>("buildFatJar${flavor.capitalized()}") {
        group = "application"

        manifest {
            attributes["Main-Class"] = mainClass
        }

        archiveBaseName.set(jarName)

        from(main.output.classesDirs)

        this.duplicatesStrategy = duplicatesStrategy

        val dependencies = main.runtimeDependencyFiles.map {
            if (it.isDirectory) it else project.zipTree(it)
        }
        from(dependencies)

        exclude("META-INF/LICENSE")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/NOTICE")
        exclude("META-INF/versions/**")
        exclude("META-INF/*.kotlin_module")
    }
}
