package io.github.philkes.android.auto.translation.provider

import java.util.Locale

abstract class TranslationService {

    /** Translate a batch of texts; implementations may optimize roundtrips. */
    protected abstract fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
    ): List<String>

    /** Convert the given `Locale` to the API specific String */
    protected open fun localeToApiString(locale: Locale): String {
        return locale.toString()
    }

    /** Public helper to expose provider-specific API code for a Locale */
    fun toApiString(locale: Locale): String = localeToApiString(locale)

    /** Translate with explicit API language codes (bypassing locale conversion) */
    fun translateBatchWithApiCodes(
        texts: List<String>,
        sourceLanguageApi: String,
        targetLanguageApi: String,
    ): List<String> {
        return translateBatch(texts, sourceLanguageApi, targetLanguageApi)
    }

    fun translateBatch(
        texts: List<String>,
        sourceLanguage: Locale,
        targetLanguage: Locale,
    ): List<String> {
        return translateBatch(
            texts,
            localeToApiString(sourceLanguage),
            localeToApiString(targetLanguage),
        )
    }
}
