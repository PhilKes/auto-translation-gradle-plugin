package io.github.philkes.android.auto.translation.provider

import com.deepl.api.DeepLClient
import com.deepl.api.TextResult

class DeepLTranslationService(private val client: DeepLClient) : TranslationService {

    constructor(apiKey: String) : this(DeepLClient(apiKey))

    override fun translate(text: String, sourceLanguage: String, targetLanguage: String): String {
        val result: TextResult = client.translateText(text, sourceLanguage, targetLanguage)
        return result.text
    }

    override fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): List<String> {
        val results: List<TextResult> = client.translateText(texts, sourceLanguage, targetLanguage)
        return results.map { it.text }
    }
}
