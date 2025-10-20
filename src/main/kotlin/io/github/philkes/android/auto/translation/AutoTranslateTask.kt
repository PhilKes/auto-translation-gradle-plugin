package io.github.philkes.android.auto.translation

import io.github.philkes.android.auto.translation.provider.DeepLTranslationService
import io.github.philkes.android.auto.translation.provider.GoogleTranslationService
import io.github.philkes.android.auto.translation.provider.TranslationProvider
import io.github.philkes.android.auto.translation.provider.TranslationService
import io.github.philkes.android.auto.translation.util.StringsXmlHelper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

/**
 * Translates all missing strings.xml Strings via external translation provider.
 */
abstract class AutoTranslateTask @Inject constructor() : DefaultTask() {

    /**
     * Provider service for the translation
     */
    @get:Input
    abstract val provider: Property<TranslationProvider>

    /**
     * API Key used for authentication for the selected `provider`
     */
    @get:Input
    @get:Optional
    abstract val apiKey: Property<String>

    /**
     * Language of the source strings (`src/main/res/values/strings.xml`).
     * Defaults to: 'en'
     */
    @get:Input
    @get:Optional
    abstract val sourceLanguage: Property<String>

    /**
     * Language ISO-Codes (from `values-{iso-code}` folder names) to translate.
     * By defaults detects all available langauges from the `src/main/res` folder.
     */
    @get:Input
    @get:Optional
    abstract val targetLanguages: ListProperty<String>

    /**
     * Path to the folder containing the `values-{iso-code}` folders.
     * Defaults to `${projectDir}/src/main/res`
     */
    @get:InputDirectory
    @get:Optional
    abstract val resDir: DirectoryProperty

    // Optional provider override for testing via secondary constructor
    private var overrideProvider: TranslationService? = null

    // Secondary constructor intended for tests to override provider
    constructor(translationService: TranslationService) : this() {
        this.overrideProvider = translationService
    }

    // Setter to override provider in tests when default constructor is used
    fun setTranslationProviderForTesting(provider: TranslationService) {
        this.overrideProvider = provider
    }

    init {
        description = "Auto-translate Android strings.xml files"
        group = "translations"
        // Defaults
        resDir.convention(project.layout.projectDirectory.dir("src/main/res"))
        sourceLanguage.convention("en")
        targetLanguages.convention(emptyList())
    }

    @TaskAction
    fun translate() {
        val res = resDir.get().asFile
        val chosenProvider = provider.getOrNull()
        if (chosenProvider == null) {
            throw GradleException("No 'provider' configured. Supported providers: ${TranslationProvider.values().toList()}")
        }
        val langs = resolveTargetLanguages(res)
        if (langs.isEmpty()) {
            logger.lifecycle("No target languages resolved. Nothing to translate.")
            return
        }
        val xml = StringsXmlHelper(logger)
        val translationService: TranslationService = overrideProvider ?: when (chosenProvider) {
            TranslationProvider.DEEPL -> {
                val key = apiKey.orNull
                if (key.isNullOrBlank()) {
                    throw GradleException("DeepL provider selected but no API key provided. Set autoTranslate.apiKey.")
                } else {
                    DeepLTranslationService(key)
                }
            }
            TranslationProvider.GOOGLE -> {
                val key = apiKey.orNull
                if (key.isNullOrBlank()) {
                    throw GradleException("Google provider selected but no API key provided. Set autoTranslate.apiKey.")
                } else {
                    GoogleTranslationService(key)
                }
            }
        }
        logger.log(LogLevel.LIFECYCLE, "AutoTranslateTask: provider=$chosenProvider, resDir=${res.absolutePath}, targetLanguages=$langs")
        val baseDir = File(res, "values")
        val baseFile = File(baseDir, "strings.xml")
        if (!baseFile.exists()) {
            throw GradleException("Base strings.xml not found at ${baseFile.absolutePath}. Ensure your project contains src/main/res/values/strings.xml or configure resDir accordingly.")
        }

        val baseStrings = xml.parse(baseFile)
        if (baseStrings.isEmpty()) {
            logger.lifecycle("No translatable <string> entries found in base strings.xml. Nothing to do.")
            return
        }

        langs.forEach { lang ->
            val targetDir = File(res, "values-$lang")
            val targetFile = File(targetDir, "strings.xml")

            val existing = if (targetFile.exists()) xml.parse(targetFile) else emptyMap()
            val missingKeys = baseStrings.keys.filter { it !in existing.keys }
            if (missingKeys.isEmpty()) {
                logger.lifecycle("[$lang] All strings already present (${existing.size}). Skipping.")
                return@forEach
            }

            val textsToTranslate = missingKeys.map { key -> baseStrings[key] ?: "" }
            val translated: List<String> = try {
                translationService.translateBatch(textsToTranslate, sourceLanguage.getOrElse("en"), lang)
            } catch (e: Exception) {
                logger.error("[$lang] Translation failed: ${e.message}", e)
                return@forEach
            }

            val additions = missingKeys.zip(translated).toMap()
            val merged = LinkedHashMap<String, String>().apply {
                putAll(existing)
                additions.forEach { (k, v) -> this[k] = v }
            }

            if (!targetDir.exists()) targetDir.mkdirs()
            xml.write(targetFile, merged)
            logger.lifecycle("[$lang] Wrote ${additions.size} new translations. Total strings now: ${merged.size}.")
        }
    }

    private fun resolveTargetLanguages(res: File): List<String> {
        val explicit = targetLanguages.getOrElse(emptyList())
        if (explicit.isNotEmpty()) return explicit

        val locales = mutableSetOf<String>()
        if (res.exists()) {
            res.listFiles()?.forEach { dir ->
                if (dir.isDirectory && dir.name.startsWith("values-")) {
                    val qualifier = dir.name.removePrefix("values-")
                    if (qualifier.isNotBlank()) {
                        // Take the first segment as language code (e.g., "en", "de", "pt")
                        val lang = qualifier.split("-", limit = 2).first()
                        locales.add(lang)
                    }
                }
            }
        }
        return locales.sorted()
    }

}