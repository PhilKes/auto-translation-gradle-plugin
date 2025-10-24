package io.github.philkes.android.auto.translation.task

import io.github.philkes.android.auto.translation.config.AzureConfig
import io.github.philkes.android.auto.translation.config.DeepLConfig
import io.github.philkes.android.auto.translation.config.FastlaneTranslationConfig
import io.github.philkes.android.auto.translation.config.GoogleConfig
import io.github.philkes.android.auto.translation.config.ProviderConfig
import io.github.philkes.android.auto.translation.provider.AzureTranslationService
import io.github.philkes.android.auto.translation.provider.DeepLTranslationService
import io.github.philkes.android.auto.translation.provider.GoogleTranslationService
import io.github.philkes.android.auto.translation.provider.TestTranslationService
import io.github.philkes.android.auto.translation.provider.TranslationService
import io.github.philkes.android.auto.translation.util.StringsXmlHelper
import io.github.philkes.android.auto.translation.util.androidCode
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
     * `en-US` (English USA)
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

    // TOOD: Add ignores for values folders and fastlane metadata files

    /** Fastlane translation configuration wrapper. */
    @get:Nested @get:Optional abstract val fastlane: Property<FastlaneTranslationConfig>

    /** Specify which translation provider to use and set it's options. */
    @get:Nested abstract val provider: Property<ProviderConfig>

    @get:OutputFiles abstract val changedStringsXmls: ListProperty<File>

    init {
        description = "Auto-translate Android strings.xml files"
        group = "translations"
        // Defaults
        resDirectory.convention(project.layout.projectDirectory.dir("src/main/res"))
        sourceLanguage.convention("en-US")
        targetLanguages.convention(emptyList())
        changedStringsXmls.set(
            project.provider {
                val targets =
                    resolveTargets(resDirectory.get().asFile, targetLanguages.get(), false)
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
        val targets = resolveTargets(valuesParentFolder, targetLanguages.get())
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
                    translationService.translateBatch(maskedTexts, srcLang, locale)
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

        // Fastlane metadata translation (optional via wrapper config)
        fastlane.orNull?.let { fastlaneConfig ->
            fastlaneConfig.setDefaultValues(project, this)
            if (fastlaneConfig.enabled.get()) {
                val metaDir = fastlaneConfig.metadataDirectory.get().asFile
                val fastlaneSrcLang = sourceLanguage.get().toIsoLocale()
                if (fastlaneSrcLang == null) {
                    throw GradleException(
                        "Non ISO Code 'fastlaneSourceLanguage' provided: '${fastlaneConfig.sourceLanguage.orNull}'"
                    )
                }
                FastlaneTranslator(logger)
                    .translate(
                        metadataRoot = metaDir,
                        service = translationService,
                        srcLang = fastlaneSrcLang,
                        targetLanguages = targetLanguages.getOrElse(emptySet()),
                    )
            }
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
    ): Map<Locale, File> {
        fun pairsFrom(codes: Collection<String>): List<Locale> =
            codes.sorted().mapNotNull { code ->
                val locale = code.toIsoLocale()
                if (locale == null) {
                    if (log)
                        logger.warn(
                            "[$code] found non ISO Code targetLanguage: '$code', will skip translation for it"
                        )
                    null
                } else {
                    locale
                }
            }
        if (languages.isNotEmpty()) {
            return pairsFrom(languages).associateWith { locale ->
                File(valuesParentFolder, "values-${locale.androidCode}/strings.xml")
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
            pairsFrom(codes).associateWith { locale ->
                File(valuesParentFolder, "values-${locale.androidCode}/strings.xml")
            }
        } else mapOf()
    }
}
