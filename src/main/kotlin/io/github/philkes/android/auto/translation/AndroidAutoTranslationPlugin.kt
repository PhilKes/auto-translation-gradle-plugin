package io.github.philkes.android.auto.translation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register



class AndroidAutoTranslationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register<AutoTranslateTask>(AUTO_TRANSLATE_TASK)
    }

    companion object{
         const val AUTO_TRANSLATE_TASK = "autoTranslate"
    }
}

