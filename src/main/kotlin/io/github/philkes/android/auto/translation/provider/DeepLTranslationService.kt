package io.github.philkes.android.auto.translation.provider

import com.deepl.api.DeepLClient
import com.deepl.api.TextResult
import com.deepl.api.TextTranslationOptions
import io.github.philkes.android.auto.translation.config.DeepLConfig
import java.util.Locale

class DeepLTranslationService(
    private val client: DeepLClient,
    private val options: TextTranslationOptions,
) : TranslationService() {

    constructor(
        config: DeepLConfig
    ) : this(
        DeepLClient(config.authKey.get()),
        config.options.getOrElse(TextTranslationOptions().setPreserveFormatting(true)).apply {
            if (tagHandling.isNullOrBlank()) {
                setTagHandling("xml")
            }
        },
    )

    override fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
    ): List<String> {
        val results: List<TextResult> =
            client.translateText(texts, sourceLanguage, targetLanguage, options)
        return results.map { it.text }
    }

    /** See https://developers.deepl.com/docs/getting-started/supported-languages */
    override fun localeToApiString(locale: Locale): String {
        return when (locale) {
            Locale.SIMPLIFIED_CHINESE -> "ZH-HANS"
            Locale.TRADITIONAL_CHINESE -> "ZH-HANT"
            else ->
                ("${locale.language}${"-${locale.country}".takeIf { !locale.country.isNullOrBlank() } ?: ""}")
                    .uppercase()
        }
    }
}
