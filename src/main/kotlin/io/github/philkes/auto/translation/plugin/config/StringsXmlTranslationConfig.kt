package io.github.philkes.auto.translation.plugin.config

import io.github.philkes.auto.translation.plugin.task.AutoTranslateTask
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
    @get:Input @get:Optional val enabled: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Path to the folder containing the `values/strings.xml` and `values-{targetLanguage}` folders.
     *
     * Defaults to `${projectDir}/src/main/res`.
     */
    @get:InputDirectory
    @get:Optional
    val resDirectory: DirectoryProperty = objects.directoryProperty()

    companion object {

        internal fun default(
            objects: ObjectFactory,
            project: Project,
            task: AutoTranslateTask,
        ): StringsXmlTranslationConfig {
            return StringsXmlTranslationConfig(objects).apply {
                enabled.convention(true)
                resDirectory.convention(
                    enabled.map {
                        if (it) {
                            project.layout.projectDirectory.dir("src/main/res")
                        } else project.layout.projectDirectory
                    }
                )
            }
        }
    }
}
