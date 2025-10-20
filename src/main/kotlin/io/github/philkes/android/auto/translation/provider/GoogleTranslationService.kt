package io.github.philkes.android.auto.translation.provider

import com.google.auth.ApiKeyCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translate.TranslateOption

class GoogleTranslationService(private val service: Translate) : TranslationService {

    constructor(apiKey: String) : this(
        TranslateOptions.newBuilder().setCredentials(ApiKeyCredentials.create(apiKey)).build().service
    )

    override fun translate(text: String, sourceLanguage: String, targetLanguage: String): String {
        val result = service.translate(
            text,
            TranslateOption.sourceLanguage(sourceLanguage),
            TranslateOption.targetLanguage(targetLanguage)
        )
        return result.translatedText
    }

    override fun translateBatch(texts: List<String>, sourceLanguage: String, targetLanguage: String): List<String> {
        val result = service.translate(
            texts,
            TranslateOption.sourceLanguage(sourceLanguage),
            TranslateOption.targetLanguage(targetLanguage)
        )
        return result.map { it.translatedText }
    }
}
