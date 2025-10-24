package io.github.philkes.auto.translation.plugin.provider

import java.util.Locale

abstract class TranslationService {

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
}
