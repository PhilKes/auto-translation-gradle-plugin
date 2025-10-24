package io.github.philkes.android.auto.translation

import io.github.philkes.android.auto.translation.config.DeepLConfig
import io.github.philkes.android.auto.translation.task.AutoTranslateTask
import io.github.philkes.android.auto.translation.util.setIsUnitTest
import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class AutoTranslateTaskExcludeLanguagesTest {

    @Test
    fun `autodetect excludes specified languages`(@TempDir projectDir: File) {
        // Given base strings and two target folders present (de, fr)
        val baseValues = File(projectDir, "src/main/res/values").apply { mkdirs() }
        File(baseValues, "strings.xml")
            .writeText(
                """
                <resources>
                  <string name="hello">Hello</string>
                </resources>
                """
                    .trimIndent()
            )
        // Create empty target folders to be autodetected
        File(projectDir, "src/main/res/values-de").mkdirs()
        val frDir = File(projectDir, "src/main/res/values-fr").apply { mkdirs() }

        setIsUnitTest(true)
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val task = project.tasks.create("autoTranslateExclude", AutoTranslateTask::class.java)
        val config = project.objects.newInstance(DeepLConfig::class.java)
        config.authKey.set("dummy-key")
        task.provider.set(config)
        // Do NOT set targetLanguages to trigger autodetect
        task.excludeLanguages.set(setOf("fr"))

        // Configure strings.xml wrapper resDirectory
        val stringsCfg =
            project.objects.newInstance(
                io.github.philkes.android.auto.translation.config.StringsXmlTranslationConfig::class
                    .java
            )
        stringsCfg.resDirectory.set(project.layout.projectDirectory.dir("src/main/res"))
        task.translateStringsXml.set(stringsCfg)

        // When
        task.translate()

        // Then: DE should be created, FR should be excluded
        val deStrings = File(projectDir, "src/main/res/values-de/strings.xml")
        assertTrue(deStrings.exists(), "Expected DE strings.xml to be generated")
        val frStrings = File(frDir, "strings.xml")
        // FR should not be created/modified by translation step
        assertFalse(frStrings.exists(), "FR strings.xml should not be generated due to exclusion")
    }
}
