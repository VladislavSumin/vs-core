package ru.vs.build_script

import org.gradle.api.Plugin
import org.gradle.api.Project

class EmptyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // no action
    }
}
