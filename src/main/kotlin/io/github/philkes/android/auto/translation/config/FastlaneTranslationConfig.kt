package io.github.philkes.android.auto.translation.config

import io.github.philkes.android.auto.translation.task.AutoTranslateTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional

/** Configuration for translating Fastlane metadata files. */
open class FastlaneTranslationConfig @Inject constructor(objects: ObjectFactory) {

    /**
     * Whether to translate Fastlane metadata text files.
     *
     * Defaults to `false`.
     */
    @get:Input @get:Optional val enabled: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Path to Fastlane metadata root directory (contains locale subfolders like `en-US`).
     *
     * Defaults to `${projectDir}/fastlane/metadata/android`.
     */
    @get:InputDirectory
    @get:Optional
    val metadataDirectory: DirectoryProperty = objects.directoryProperty()

    /**
     * Language ISO-Code of the source fastlane files (folder name under metadataDirectory).
     *
     * Defaults to the task's sourceLanguage ([AutoTranslateTask.sourceLanguage])
     */
    @get:Input
    @get:Optional
    val sourceLanguage: Property<String> = objects.property(String::class.java)

    /**
     * Language ISO-Codes (from '[metadataDirectory]/{targetLanguage}' folder names) to translate.
     *
     * Defaults to task's targetLanguages ([AutoTranslateTask.targetLanguages])
     */
    @get:Input
    @get:Optional
    val targetLanguages: SetProperty<String> = objects.setProperty(String::class.java)
}

internal fun FastlaneTranslationConfig.setDefaultValues(project: Project, task: AutoTranslateTask) {
    enabled.convention(false)
    metadataDirectory.convention(
        project.provider {
            if (enabled.get()) {
                project.layout.projectDirectory.dir("fastlane/metadata/android")
            } else null
        }
    )
    sourceLanguage.convention(task.sourceLanguage)
    targetLanguages.convention(task.targetLanguages)
}
