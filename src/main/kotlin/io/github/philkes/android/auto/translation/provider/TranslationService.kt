package io.github.philkes.android.auto.translation.provider

interface TranslationService {
    /**
     * Translate a single text from sourceLanguage to targetLanguage.
     * If sourceLanguage is null, the provider may auto-detect.
     */
    fun translate(text: String, sourceLanguage: String, targetLanguage: String): String

    /**
     * Translate a batch of texts; implementations may optimize roundtrips.
     */
    fun translateBatch(texts: List<String>, sourceLanguage: String, targetLanguage: String): List<String> =
        texts.map { translate(it, sourceLanguage, targetLanguage) }
}
