package io.github.philkes.android.auto.translation

import com.deepl.api.DeepLClient
import com.deepl.api.TextResult
import io.github.philkes.android.auto.translation.provider.DeepLTranslationService
import io.mockk.every
import io.mockk.mockk
import io.github.philkes.android.auto.translation.provider.TranslationProvider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AutoTranslateTaskIntegrationTest {

    @Test
    fun `task writes translated strings using mocked DeepL client`(@TempDir projectDir: File) {
        // Base strings.xml
        val baseValues = File(projectDir, "src/main/res/values").apply { mkdirs() }
        writeFile(
            File(baseValues, "strings.xml"),
            """
            <resources>
              <string name="hello">Hello</string>
              <string name="bye">Bye</string>
              <string name="skip_me" translatable="false">DO NOT TRANSLATE</string>
            </resources>
            """.trimIndent()
        )

        // Existing German with one key to test merge behavior
        val deValues = File(projectDir, "src/main/res/values-de").apply { mkdirs() }
        writeFile(
            File(deValues, "strings.xml"),
            """
            <resources>
              <string name="hello">Hallo</string>
            </resources>
            """.trimIndent()
        )

        // Create a mocked DeepL client that appends [<target>] to each text
        val mockClient = mockk<DeepLClient>()
        every { mockClient.translateText(any<List<String>>(), "en", any<String>()) } answers {
            val texts = firstArg<List<String>>()
            val target = arg<String>(2)
            texts.map { t ->
                val result = mockk<TextResult>()
                every { result.text } returns "$t [$target]"
                result
            }
        }

        val provider = DeepLTranslationService(mockClient)

        // Build a minimal Gradle task via ProjectBuilder
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val task = project.tasks.create("autoTranslate", AutoTranslateTask::class.java)
        task.setTranslationProviderForTesting(provider)
        task.provider.set(TranslationProvider.DEEPL)
        task.targetLanguages.set(listOf("de", "fr"))
        task.resDir.set(project.layout.projectDirectory.dir("src/main/res"))

        // Execute the task
        task.translate()

        // Verify DE file merged: existing 'hello' preserved, 'bye' added by mock provider
        val deStrings = File(deValues, "strings.xml")
        assertTrue(deStrings.exists())
        val deContent = deStrings.readText()
        assertTrue(deContent.contains("<string name=\"hello\">Hallo</string>"))
        assertTrue(deContent.contains("<string name=\"bye\">Bye [de]</string>"))
        // Ensure translatable=false key not added
        assertTrue(!deContent.contains("skip_me"))

        // Verify FR file created with both keys translated by mock provider
        val frStrings = File(projectDir, "src/main/res/values-fr/strings.xml")
        assertTrue(frStrings.exists())
        val frContent = frStrings.readText()
        assertTrue(frContent.contains("<string name=\"hello\">Hello [fr]</string>"))
        assertTrue(frContent.contains("<string name=\"bye\">Bye [fr]</string>"))
    }

    private fun writeFile(file: File, content: String) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }
}
