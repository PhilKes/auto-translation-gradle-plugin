package io.github.philkes.android.auto.translation.provider

import com.azure.ai.translation.text.TextTranslationClient
import com.azure.ai.translation.text.TextTranslationClientBuilder
import com.azure.ai.translation.text.models.TranslateOptions
import com.azure.core.credential.AzureKeyCredential


class AzureTranslationService(private val service: TextTranslationClient) : TranslationService {

    constructor(apiKey: String, region: String?, endpoint: String?) : this(
        TextTranslationClientBuilder()
            .credential(AzureKeyCredential(apiKey))
            .apply {
                if (!region.isNullOrBlank()) this.region(region)
                if (!endpoint.isNullOrBlank()) this.endpoint(endpoint)
            }
            .buildClient()
    )

    override fun translate(text: String, sourceLanguage: String, targetLanguage: String): String {
        val translateOptions: TranslateOptions = TranslateOptions()
            .setSourceLanguage(sourceLanguage)
            .addTargetLanguage(targetLanguage)

        val translation = service.translate(text, translateOptions)
        return translation.translations.first().text
    }

    override fun translateBatch(texts: List<String>, sourceLanguage: String, targetLanguage: String): List<String> {
        val translateOptions: TranslateOptions = TranslateOptions()
            .setSourceLanguage(sourceLanguage)
            .addTargetLanguage(targetLanguage)
        val translations = service.translate(texts, translateOptions)
        return translations.map { it.translations.first().text }
    }
}
