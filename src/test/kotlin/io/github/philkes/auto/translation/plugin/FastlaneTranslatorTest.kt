package io.github.philkes.auto.translation.plugin

import io.github.philkes.auto.translation.plugin.provider.TestTranslationService
import io.github.philkes.auto.translation.plugin.task.FastlaneTranslator
import io.github.philkes.auto.translation.plugin.util.setIsUnitTest
import io.github.philkes.auto.translation.plugin.util.toIsoLocale
import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FastlaneTranslatorTest {

    @Test
    fun `translates fastlane autodetected targets`(@TempDir projectDir: File) {
        // Given a fastlane structure with source en-US and target de-DE
        val fastlaneRoot = File(projectDir, "fastlane/metadata/android").apply { mkdirs() }
        val srcDir = File(fastlaneRoot, "en-US").apply { mkdirs() }
        val deDir = File(fastlaneRoot, "de-DE").apply { mkdirs() }
        File(srcDir, "title.txt").writeText("Hello App")
        File(srcDir, "short_description.txt").apply {
            parentFile.mkdirs()
            writeText("This is the short description")
        }

        // When
        setIsUnitTest(true)
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val logger = project.logger
        val translator = FastlaneTranslator(logger)
        val service = TestTranslationService()
        translator.translate(
            metadataRoot = fastlaneRoot,
            service = service,
            srcLang = "en-US".toIsoLocale()!!,
            targetLanguages = emptySet(),
        )

        // Then: files are written in de-DE and contents suffixed with [DE_DE] by
        // TestTranslationService
        val deTitle = File(deDir, "title.txt").readText()
        assertEquals("Hello App [DE_DE]", deTitle)
        val deShort = File(deDir, "short_description.txt").readText()
        assertEquals("This is the short description [DE_DE]", deShort)
    }
}
