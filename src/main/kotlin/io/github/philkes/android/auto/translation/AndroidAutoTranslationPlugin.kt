package io.github.philkes.android.auto.translation

import io.github.philkes.android.auto.translation.config.AutoTranslationExtension
import io.github.philkes.android.auto.translation.task.AutoTranslateTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoTranslationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create(AUTO_TRANSLATE_TASK, AutoTranslationExtension::class.java)

        project.tasks.register(
            AUTO_TRANSLATE_TASK,
            AutoTranslateTask::class.java,
            object : Action<AutoTranslateTask> {
                override fun execute(task: AutoTranslateTask) {
                    extension.sourceLanguage.orNull?.let { task.sourceLanguage.set(it) }
                    extension.targetLanguages.orNull?.let { task.targetLanguages.set(it) }
                    extension.excludeLanguages.orNull?.let { task.excludeLanguages.set(it) }
                    extension.provider.orNull?.let { task.provider.set(it) }
                    extension.translateStringsXml.orNull?.let { task.translateStringsXml.set(it) }
                    extension.translateFastlane.orNull?.let { task.translateFastlane.set(it) }
                }
            },
        )
    }

    companion object {
        const val AUTO_TRANSLATE_TASK = "androidAutoTranslate"
    }
}
