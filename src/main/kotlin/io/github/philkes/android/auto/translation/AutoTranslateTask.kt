package io.github.philkes.android.auto.translation

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AutoTranslateTask : DefaultTask() {

    init {
        description = "Auto. translates all strings.xml"
        group = "translations"
    }

    @TaskAction
    fun export() {

    }
}