package io.github.philkes.auto.translation.plugin.provider.libretranslate

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.philkes.auto.translation.plugin.config.LibreTranslateConfig
import io.github.philkes.auto.translation.plugin.provider.TextFormat
import io.github.philkes.auto.translation.plugin.provider.TranslationService
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

class LibreTanslateTranslationService(private val client: LibreTranslateClient) :
    TranslationService() {

    constructor(config: LibreTranslateConfig) : this(LibreTranslateClient(config))

    override fun translateBatch(
        texts: List<String>,
        textFormat: TextFormat,
        sourceLanguage: String,
        targetLanguage: String,
    ): List<String> {
        // TODO: is there text format option?
        return texts.map { client.translate(it, sourceLanguage, targetLanguage).translatedText }
    }
}

data class TranslateRequest(
    val q: String,
    val source: String,
    val target: String,
    val format: String?,
    @JsonProperty("api_key") val apiKey: String? = null,
)

data class TranslateResponse(val translatedText: String)

/** See https://docs.libretranslate.com/guides/api_usage/ */
class LibreTranslateClient(private val config: LibreTranslateConfig) {
    private val httpClient: CloseableHttpClient = HttpClients.createDefault()
    private val mapper = ObjectMapper()

    fun translate(text: String, source: String, target: String): TranslateResponse {
        val url = "${config.baseUrl.get().removeSuffix("/")}/translate"
        val requestBody = TranslateRequest(text, source, target, "html", config.apiKey.orNull)
        val json = mapper.writeValueAsString(requestBody)

        val request =
            HttpPost(url).apply { entity = StringEntity(json, ContentType.APPLICATION_JSON) }

        return httpClient.execute(request).use { response ->
            val body = response.entity.content.bufferedReader().use { it.readText() }
            mapper.readValue(body, TranslateResponse::class.java)
        }
    }
}
