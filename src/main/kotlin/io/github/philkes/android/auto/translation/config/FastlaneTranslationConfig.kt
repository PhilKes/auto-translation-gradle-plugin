package io.github.philkes.android.auto.translation.config

import io.github.philkes.android.auto.translation.task.AutoTranslateTask
import org.gradle.api.Project
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

/** Configuration for translating Fastlane metadata files. */
open class FastlaneTranslationConfig @Inject constructor(objects: ObjectFactory) {

    /** Whether to translate Fastlane metadata text files. Defaults to false. */
    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Path to Fastlane metadata root directory (contains locale subfolders like `en-US`). Defaults
     * to `${projectDir}/fastlane/metadata/android`.
     */
    @get:InputDirectory
    @get:Optional
    val metadataDirectory: DirectoryProperty = objects.directoryProperty()

    /**
     * Language ISO-Code of the source fastlane files (folder name under metadataDirectory).
     * Defaults to the task's sourceLanguage when used; if set here, overrides that default.
     */
    @get:Input
    @get:Optional
    val sourceLanguage: Property<String> = objects.property(String::class.java)

    @Internal
    internal fun setDefaultValues(project: Project, task: AutoTranslateTask) {
        enabled.convention(false)
        metadataDirectory.convention(project.layout.projectDirectory.dir("fastlane/metadata/android"))
        sourceLanguage.convention(task.sourceLanguage)
    }
}
