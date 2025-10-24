package io.github.philkes.android.auto.translation.task

import io.github.philkes.android.auto.translation.config.AzureConfig
import io.github.philkes.android.auto.translation.config.DeepLConfig
import io.github.philkes.android.auto.translation.config.FastlaneTranslationConfig
import io.github.philkes.android.auto.translation.config.GoogleConfig
import io.github.philkes.android.auto.translation.config.ProviderConfig
import io.github.philkes.android.auto.translation.config.StringsXmlTranslationConfig
import io.github.philkes.android.auto.translation.config.setDefaultValues
import io.github.philkes.android.auto.translation.provider.AzureTranslationService
import io.github.philkes.android.auto.translation.provider.DeepLTranslationService
import io.github.philkes.android.auto.translation.provider.GoogleTranslationService
import io.github.philkes.android.auto.translation.provider.TestTranslationService
import io.github.philkes.android.auto.translation.provider.TranslationService
import io.github.philkes.android.auto.translation.util.isUnitTest
import io.github.philkes.android.auto.translation.util.readableClassName
import io.github.philkes.android.auto.translation.util.toIsoLocale
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

/** Translates all missing strings.xml Strings via external translation provider. */
abstract class AutoTranslateTask @Inject constructor() : DefaultTask() {

    /**
     * Language ISO-Code of the source strings (`src/main/res/values/strings.xml`).
     *
     * Defaults to: `en-US` (English USA)
     */
    @get:Input @get:Optional abstract val sourceLanguage: Property<String>

    /**
     * Language ISO-Codes (e.g.: `en-US`) to translate.
     *
     * Note that Android uses a different formatting for the country/region value in their `values` folder naming:
     * `values-{languageCode}-r{countryCode}`, whereas the ISO-Code uses: `{languageCode-countrCode}`.
     *
     * By default detects all available targetLanguages from the [StringsXmlTranslationConfig.resDirectory] `values` folders.
     */
    @get:Input @get:Optional abstract val targetLanguages: SetProperty<String>

    /**
     * strings.xml translation configuration wrapper.
     *
     * By default strings.xml translation is enabled
     */
    @get:Nested @get:Optional abstract val translateStringsXml: Property<StringsXmlTranslationConfig>

    // TOOD: Add ignores for values folders and fastlane metadata files

    /** Fastlane translation configuration wrapper. */
    @get:Nested @get:Optional abstract val translateFastlane: Property<FastlaneTranslationConfig>

    /** Specify which translation provider to use and set it's options. */
    @get:Nested abstract val provider: Property<ProviderConfig>

    @get:OutputFiles abstract val changedStringsXmls: ListProperty<File>

    init {
        description = "Auto-translate Android strings.xml files"
        group = "translations"
        // Defaults
        sourceLanguage.convention("en-US")
        targetLanguages.convention(emptySet<String>())
        translateStringsXml.convention(
            project.provider {
                val cfg = project.objects.newInstance(StringsXmlTranslationConfig::class.java)
                cfg.setDefaultValues(project, this)
                cfg
            }
        )
        translateFastlane.convention(
            project.provider {
                val cfg = project.objects.newInstance(FastlaneTranslationConfig::class.java)
                cfg.setDefaultValues(project, this)
                cfg
            }
        )
        changedStringsXmls.set(
            project.provider {
                val cfg = translateStringsXml.get()
                cfg.setDefaultValues(project, this)
                val resDir = cfg.resDirectory.get().asFile
                StringsXmlTranslator(logger)
                    .resolveTargets(resDir, targetLanguages.getOrElse(emptySet()), false)
                    .map { it.value }
            }
        )
    }

    @TaskAction
    fun translate() {
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
        val translationService: TranslationService = createTranslationService(provider)

        // Strings.xml translation via wrapper config (enabled by default)
        translateStringsXml.orNull?.let { cfg ->
            if (cfg.enabled.get()) {
                val resDir = cfg.resDirectory.get().asFile
                if(!resDir.exists()){
                    throw GradleException("Provided translateStringsXml 'resDirectory' does not exist: $resDir")
                }
                logger.log(
                    LogLevel.LIFECYCLE,
                    "StringsXmlTranslation: provider=${provider.readableClassName}, resDirectory=${resDir.absolutePath}, sourceLanguage=$srcLang, targetLanguages=${targetLanguages.getOrElse(emptySet())}",
                )
                StringsXmlTranslator(logger)
                    .translate(
                        resDirectory = resDir,
                        service = translationService,
                        srcLang = srcLang,
                        targetLanguages = targetLanguages.get(),
                    )
            }
        }

        // Fastlane metadata translation (optional via wrapper config)
        translateFastlane.orNull?.let { fastlaneConfig ->
            if (fastlaneConfig.enabled.get()) {
                val metaDir = fastlaneConfig.metadataDirectory.get().asFile
                if(!metaDir.exists()){
                    throw GradleException("Provided translateFastlane 'metadataDirectory' does not exist: $metaDir")
                }
                val fastlaneSrcLang = fastlaneConfig.sourceLanguage.get().toIsoLocale()
                if (fastlaneSrcLang == null) {
                    throw GradleException(
                        "Non ISO Code fastlaneConfig 'sourceLanguage' provided: '${fastlaneConfig.sourceLanguage.orNull}'"
                    )
                }
                val fastlaneTargetLanguages = fastlaneConfig.targetLanguages.get()
                logger.log(
                    LogLevel.LIFECYCLE,
                    "StringsXmlTranslation: provider=${provider.readableClassName}, metadataDirectory=${metaDir}, sourceLanguage=$srcLang, targetLanguages=$fastlaneTargetLanguages",
                )
                FastlaneTranslator(logger)
                    .translate(
                        metadataRoot = metaDir,
                        service = translationService,
                        srcLang = fastlaneSrcLang,
                        targetLanguages = fastlaneTargetLanguages,
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
}
