package io.github.philkes.android.auto.translation.task

import io.github.philkes.android.auto.translation.config.AzureConfig
import io.github.philkes.android.auto.translation.config.DeepLConfig
import io.github.philkes.android.auto.translation.config.GoogleConfig
import io.github.philkes.android.auto.translation.config.ProviderConfig
import io.github.philkes.android.auto.translation.provider.AzureTranslationService
import io.github.philkes.android.auto.translation.provider.DeepLTranslationService
import io.github.philkes.android.auto.translation.provider.GoogleTranslationService
import io.github.philkes.android.auto.translation.provider.TestTranslationService
import io.github.philkes.android.auto.translation.provider.TranslationService
import io.github.philkes.android.auto.translation.util.StringsXmlHelper
import io.github.philkes.android.auto.translation.util.isUnitTest
import io.github.philkes.android.auto.translation.util.readableClassName
import io.github.philkes.android.auto.translation.util.toIsoLocale
import java.io.File
import java.util.Locale
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

/** Translates all missing strings.xml Strings via external translation provider. */
abstract class AutoTranslateTask @Inject constructor() : DefaultTask() {

    /**
     * Language ISO-Code of the source strings (`src/main/res/values/strings.xml`). Defaults to:
     * `en` (English)
     */
    @get:Input @get:Optional abstract val sourceLanguage: Property<String>

    /**
     * Language ISO-Codes (from `values-{iso-code}` folder names) to translate. By defaults detects
     * all available langauges from the `src/main/res` folder.
     */
    @get:Input @get:Optional abstract val targetLanguages: SetProperty<String>

    /**
     * Path to the folder containing the `values/strings.xml` and `values-{iso-code}` folders.
     * Defaults to `${projectDir}/src/main/res`
     */
    @get:InputDirectory @get:Optional abstract val resDirectory: DirectoryProperty

    /** Specify which translation provider to use and set it's options. */
    @get:Nested abstract val provider: Property<ProviderConfig>

    /**
     * Optionally overwrite mapping from `values-{targetLanguage}` to language code used by the API.
     * E.g. if for some reason have a `values-xyz` folder which should have `strings.xml` with
     * german translation in it, you can set this to:
     */
    @get:Input @get:Optional abstract val languageCodeOverwrites: MapProperty<String, String>

    @get:OutputFiles abstract val changedStringsXmls: ListProperty<File>

    init {
        description = "Auto-translate Android strings.xml files"
        group = "translations"
        // Defaults
        resDirectory.convention(project.layout.projectDirectory.dir("src/main/res"))
        sourceLanguage.convention("en")
        targetLanguages.convention(emptyList())
        languageCodeOverwrites.convention(emptyMap<String, String>())
        changedStringsXmls.set(
            project.provider {
                val targets =
                    resolveTargets(
                        resDirectory.get().asFile,
                        targetLanguages.get(),
                        false,
                        languageCodeOverwrites.get(),
                    )
                targets.map { it.value }
            }
        )
    }

    @TaskAction
    fun translate() {
        val valuesParentFolder = resDirectory.get().asFile
        val provider = provider.get()
        if (!provider.isValid()) {
            throw GradleException(
                "Parameter 'providerConfig': ${provider.readableClassName} is invalid: ${provider.getConstraints()}"
            )
        }
        val srcLang = sourceLanguage.get().toIsoLocale()
        if (srcLang == null) {
            throw GradleException("Found non ISO Code sourceLanguage: '${sourceLanguage.get()}'")
        }
        val xml = StringsXmlHelper(logger)
        val targets =
            resolveTargets(
                valuesParentFolder,
                targetLanguages.get(),
                overwrites = languageCodeOverwrites.get(),
            )
        if (targets.isEmpty()) {
            logger.lifecycle("No targetLanguages resolved. Nothing to translate.")
            return
        } else {
            logger.lifecycle("Translating for targetLanguages: ${targets.map { it.key }}")
        }
        val translationService: TranslationService = createTranslationService(provider)
        logger.log(
            LogLevel.LIFECYCLE,
            "AutoTranslateTask: provider=${provider.readableClassName}, resDirectory=${valuesParentFolder.absolutePath}, sourceLanguage=$srcLang, targetLanguages=${targets.map { it.key }}",
        )
        val sourceDir = File(valuesParentFolder, "values")
        val sourceStringsFile = File(sourceDir, "strings.xml")
        if (!sourceStringsFile.exists()) {
            throw GradleException(
                "Source file '${sourceStringsFile.absolutePath}' does not exist. Ensure 'resDirectory' is configured correctly (currently: '${sourceDir.absolutePath}')."
            )
        }
        val sourceStrings = xml.parse(sourceStringsFile)
        if (sourceStrings.isEmpty()) {
            logger.lifecycle(
                "No translatable <string> entries found in base strings.xml. Nothing to do."
            )
            return
        }
        targets.forEach { (locale, targetStringsFile) ->
            val existing =
                if (targetStringsFile.exists()) xml.parse(targetStringsFile) else emptyMap()
            val missingKeys = sourceStrings.keys.filter { it !in existing.keys }
            if (missingKeys.isEmpty()) {
                logger.lifecycle(
                    "[$locale] All strings already present (${existing.size}). Skipping."
                )
                return@forEach
            }

            val textsToTranslate = missingKeys.map { key -> sourceStrings[key] ?: "" }

            // Protect Android printf-style placeholders (e.g., %1$s) from being altered by
            // providers
            val maskedTexts = textsToTranslate.map { xml.maskPlaceholders(it) }

            val translatedMasked =
                try {
                    val folderCode = targetStringsFile.parentFile.name.removePrefix("values-")
                    val overwriteTarget = languageCodeOverwrites.get()[folderCode]
                    if (overwriteTarget != null) {
                        val srcApi = translationService.toApiString(srcLang)
                        if (logger.isEnabled(LogLevel.INFO)) {
                            logger.info(
                                "[$locale] Using overwritten API language '$overwriteTarget' for folder '$folderCode'"
                            )
                        }
                        translationService.translateBatchWithApiCodes(
                            maskedTexts,
                            srcApi,
                            overwriteTarget,
                        )
                    } else {
                        translationService.translateBatch(maskedTexts, srcLang, locale)
                    }
                } catch (e: Exception) {
                    logger.error("[$locale] Translation failed: ${e.message}", e)
                    return@forEach
                }

            // Restore placeholders in the translated text
            val translated = translatedMasked.map { xml.unmaskPlaceholders(it) }

            val additions = missingKeys.zip(translated).toMap()
            val merged =
                LinkedHashMap<String, String>().apply {
                    putAll(existing)
                    additions.forEach { (k, v) -> this[k] = v }
                }
            val targetDir = targetStringsFile.parentFile
            if (!targetDir.exists()) targetDir.mkdirs()
            xml.write(targetStringsFile, merged)
            logger.lifecycle(
                "[$locale] Wrote ${additions.size} new translations. Total strings now: ${merged.size}."
            )
        }
    }

    private fun createTranslationService(provider: ProviderConfig): TranslationService {
        if (isUnitTest) {
            return TestTranslationService()
        }
        try {
            return when (provider) {
                is DeepLConfig -> DeepLTranslationService(provider)
                is GoogleConfig -> GoogleTranslationService(provider)
                is AzureConfig -> AzureTranslationService(provider)
            }
        } catch (e: Exception) {
            throw GradleException(
                "Configuration of Client for ${provider.readableClassName} failed: ${e.message}",
                e,
            )
        }
    }

    fun resolveTargets(
        valuesParentFolder: File,
        languages: Set<String>,
        log: Boolean = true,
        overwrites: Map<String, String> = emptyMap(),
    ): Map<Locale, File> {
        fun pairsFrom(codes: Collection<String>): List<Pair<Locale, String>> =
            codes.sorted().mapNotNull { code ->
                val localeStr = overwrites[code] ?: code
                val locale = localeStr.toIsoLocale()
                if (locale == null) {
                    if (log)
                        logger.warn(
                            "[$code] found non ISO Code targetLanguage: '$code', will skip translation for it"
                        )
                    null
                } else {
                    if (locale.toString() != code) {
                        logger.info(
                            "[$code] mapped values folder Code '$code' to overwritten language '$localeStr'"
                        )
                    }
                    Pair(locale, code)
                }
            }
        if (languages.isNotEmpty()) {
            return pairsFrom(languages).associate { (locale, code) ->
                locale to File(valuesParentFolder, "values-$code/strings.xml")
            }
        }
        if (log) logger.lifecycle("Auto. detecting targetLanguages...")
        return if (valuesParentFolder.exists()) {
            val codes =
                valuesParentFolder.listFiles()?.mapNotNull { dir ->
                    if (dir.isDirectory && dir.name.startsWith("values-"))
                        dir.name.removePrefix("values-")
                    else null
                } ?: emptyList()
            pairsFrom(codes).associate { (locale, code) ->
                locale to File(valuesParentFolder, "values-$code/strings.xml")
            }
        } else mapOf()
    }
}
