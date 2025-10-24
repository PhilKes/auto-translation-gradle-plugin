package io.github.philkes.android.auto.translation.config

import io.github.philkes.android.auto.translation.task.AutoTranslateTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional

/** Configuration for translating Android strings.xml resources. */
open class StringsXmlTranslationConfig @Inject constructor(objects: ObjectFactory) {

    /**
     * Whether to translate strings.xml resources.
     *
     * Defaults to `true`
     */
    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    /**
     * Path to the folder containing the `values/strings.xml` and `values-{targetLanguage}` folders.
     *
     * Defaults to `${projectDir}/src/main/res`.
     */
    @get:InputDirectory
    @get:Optional
    val resDirectory: DirectoryProperty = objects.directoryProperty()
}

internal fun StringsXmlTranslationConfig.setDefaultValues(
    project: Project,
    task: AutoTranslateTask,
) {
    enabled.convention(true)
    resDirectory.convention(
        project.provider {
            if (enabled.get()) {
                project.layout.projectDirectory.dir("src/main/res")
            } else null
        }
    )
}
