package io.github.philkes.android.auto.translation.provider

import com.google.cloud.translate.Translate
import io.github.philkes.android.auto.translation.config.GoogleConfig

class GoogleTranslationService(private val service: Translate, private val model: String?) :
    TranslationService() {

    constructor(
        config: GoogleConfig
    ) : this(config.options.get().build().service, config.model.orNull)

    override fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
    ): List<String> {
        var options =
            arrayOf(
                Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.format("html"),
            )
        if (!model.isNullOrBlank()) {
            options += arrayOf(Translate.TranslateOption.model(model))
        }
        val result = service.translate(texts, *options)
        return result.map { it.translatedText }
    }
}
