package io.github.philkes.auto.translation.plugin.provider.openai

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.openai.client.OpenAIClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import io.github.philkes.auto.translation.plugin.config.OpenAIConfig
import io.github.philkes.auto.translation.plugin.provider.TextFormat
import io.github.philkes.auto.translation.plugin.provider.TranslationService
import io.github.philkes.auto.translation.plugin.util.DOLLAR

class OpenAITranslationService(
    private val client: OpenAIClient,
    private val model: String,
    private val systemMessage: String,
) : TranslationService() {

    constructor(
        config: OpenAIConfig
    ) : this(
        config.options.get().toActualBuilder().build(),
        config.model.get(),
        config.systemMessage.get(),
    )

    private val objectMapper = jacksonObjectMapper()
    private val exampleMsg: String
    private val exampleResponse: String

    init {
        exampleMsg = OpenAiTranslateRequest("en", "de", listOf("Hello", "What is this?")).toJson()
        exampleResponse = OpenAiTranslateResponse(listOf("Hallo", "Was ist das?")).toJson()
    }

    override fun translateBatch(
        texts: List<String>,
        textFormat: TextFormat,
        sourceLanguage: String,
        targetLanguage: String,
    ): List<String> {
        val params =
            ChatCompletionCreateParams.builder()
                .model(model)
                .responseFormat(OpenAiTranslateResponse::class.java)
                .addSystemMessage(systemMessage)
                .addUserMessage(exampleMsg)
                .addAssistantMessage(exampleResponse)
                .addUserMessage(
                    OpenAiTranslateRequest(sourceLanguage, targetLanguage, texts).toJson()
                )
                .build()
        return client
            .chat()
            .completions()
            .create(params)
            .choices()
            .first()
            .message()
            .content()
            .get()
            .translatedTexts
    }

    data class OpenAiTranslateRequest(
        val srcLang: String,
        val targetLang: String,
        val texts: List<String>,
    )

    fun OpenAiTranslateRequest.toJson() = objectMapper.writeValueAsString(this)

    data class OpenAiTranslateResponse(val translatedTexts: List<String>)

    fun OpenAiTranslateResponse.toJson() = objectMapper.writeValueAsString(this)

    companion object {
        /** System prompt to tell ChatGPT its a translator. */
        val DEFAULT_SYSTEM_MESSAGE =
            """
                You're a professional translator for software projects, especially Android apps.
                You are given text in a specified source language, and should translate it in the most suitable way to the specified target language.
                The given texts can contain XML/HTML tags, as well as special string formatting placeholders like '%1${DOLLAR}s',
                 do not translate them and keep them at the position they were originally by preserving the format of the text.
                The input is a JSON object that contains:
                 - the texts' source language ('srcLang'),
                 - the desired target languages ('targetLangs'),
                 - the list of texts that should be translated.
            """
                .trimIndent()
    }
}
