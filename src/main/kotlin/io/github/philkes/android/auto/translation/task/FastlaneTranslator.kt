package io.github.philkes.android.auto.translation.task

import io.github.philkes.android.auto.translation.provider.TranslationService
import io.github.philkes.android.auto.translation.util.toIsoLocale
import java.io.File
import java.util.Locale
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger

/** Encapsulates translation of Fastlane metadata text files. */
class FastlaneTranslator(private val logger: Logger) {

    /**
     * Translate all .txt files from the Fastlane source locale folder into target locale folders.
     * - metadataRoot: root directory containing locale subfolders like "en-US"
     * - service: translation service to use
     * - srcLang: source locale
     * - targetLanguages: explicit target folder codes (when empty, autodetect from directories)
     * - excludeLanguages: optional list of folder codes to exclude when autodetecting
     */
    fun translate(
        metadataRoot: File,
        service: TranslationService,
        srcLang: Locale,
        targetLanguages: Set<String>,
        excludeLanguages: Set<String> = emptySet(),
    ) {
        if (!metadataRoot.exists()) {
            throw GradleException(
                "Fastlane metadata directory does not exist: ${metadataRoot.absolutePath}. Skipping."
            )
        }
        val allLocaleDirs = metadataRoot.listFiles()?.filter { it.isDirectory } ?: emptyList()
        if (allLocaleDirs.isEmpty()) {
            logger.lifecycle("No folders found in Fastlane metadata directory. Skipping.")
            return
        }
        val sourceDir = allLocaleDirs.findLocaleFolder(srcLang)
        if (sourceDir == null) {
            logger.lifecycle(
                "No Fastlane source folder matching sourceLanguage=${srcLang} found. Skipping Fastlane translation."
            )
            return
        }
        val sourceCode = sourceDir.name
        val sourceFiles = sourceDir.listTxtFilesRecursively()
        if (sourceFiles.isEmpty()) {
            logger.lifecycle(
                "No .txt files found in Fastlane source folder '${sourceDir.name}'. Nothing to translate."
            )
            return
        }

        // Determine targets: either explicit or from metadata folders
        val targetCodes: List<String> =
            if (targetLanguages.isNotEmpty()) targetLanguages.sorted()
            else allLocaleDirs.map { it.name }.filterNot { it in excludeLanguages }.sorted()

        val targets: List<Pair<Locale, String>> =
            targetCodes
                .mapNotNull { code ->
                    val loc = code.toIsoLocale()
                    if (loc == null) {
                        logger.warn("[Fastlane:$code] Not a valid ISO locale. Skipping.")
                        null
                    } else {
                        Pair(loc, code)
                    }
                }
                .filter { (_, code) -> code != sourceCode }

        // For each target, translate all files in batch preserving order and write
        targets.forEach { (targetLocale, targetCode) ->
            val contents = sourceFiles.map { it.readText() }
            val translated: List<String> =
                try {
                    service.translateBatch(contents, srcLang, targetLocale)
                } catch (e: Exception) {
                    logger.error("[Fastlane:$targetCode] Translation failed: ${e.message}", e)
                    return@forEach
                }
            // Write outputs
            translated.forEachIndexed { index, text ->
                val relative = sourceFiles[index].relativeTo(sourceDir).path
                val outFile = File(metadataRoot, "$targetCode/$relative")
                outFile.parentFile?.mkdirs()
                outFile.writeText(text)
            }
            logger.lifecycle(
                "[Fastlane:$targetCode] Wrote ${translated.size} translated .txt files."
            )
        }
    }

    private fun List<File>.findLocaleFolder(srcLang: Locale): File? {
        return firstOrNull { dir -> dir.name.toIsoLocale()?.equals(srcLang) == true }
    }

    private fun File.listTxtFilesRecursively(): List<File> {
        return walkTopDown().filter { it.isFile && it.extension == "txt" }.toList()
    }
}
