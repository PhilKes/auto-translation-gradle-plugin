package io.github.philkes.android.auto.translation.provider

import com.azure.ai.translation.text.TextTranslationClient
import com.azure.ai.translation.text.models.TextType
import com.azure.ai.translation.text.models.TranslateOptions
import io.github.philkes.android.auto.translation.config.AzureConfig

class AzureTranslationService(private val service: TextTranslationClient) : TranslationService() {

    constructor(config: AzureConfig) : this(config.options.get().buildClient())

    override fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
    ): List<String> {
        val translateOptions: TranslateOptions =
            TranslateOptions()
                .setSourceLanguage(sourceLanguage)
                .addTargetLanguage(targetLanguage)
                .setTextType(TextType.HTML)
        val translations = service.translate(texts, translateOptions)
        return translations.map { it.translations.first().text }
    }
}
