package io.github.philkes.auto.translation.plugin.provider.openai

import com.openai.azure.credential.AzureApiKeyCredential
import com.openai.client.okhttp.OpenAIOkHttpClient
import java.io.Serializable
import java.time.Duration

/**
 * A builder for creating a new instance of the OpenAIOkHttpClient type.
 *
 * Copy of
 * [OpenAIOkHttpClient.Builder] with the most important properties to make it serializable
 */
class OpenAIOkHttpClientBuilder : Serializable {
    var apiKey: String? = null
    var azureApiKey: String? = null
    var baseUrl: String? = null
    var organization: String? = null
    var project: String? = null
    var timeout: Duration? = null
    var maxRetries: Int? = null

    /**
     * @see OpenAIOkHttpClient.Builder.apiKey
     */
    fun apiKey(apiKey: String): OpenAIOkHttpClientBuilder {
        this.apiKey = apiKey
        return this
    }

    /**
     * @see OpenAIOkHttpClient.Builder.credential
     */
    fun credential(azureApiKey: String): OpenAIOkHttpClientBuilder {
        this.azureApiKey = azureApiKey
        return this
    }

    /**
     * @see OpenAIOkHttpClient.Builder.baseUrl
     */
    fun baseUrl(baseUrl: String): OpenAIOkHttpClientBuilder {
        this.baseUrl = baseUrl
        return this
    }

    /**
     * @see OpenAIOkHttpClient.Builder.organization
     */
    fun organization(organization: String): OpenAIOkHttpClientBuilder {
        this.organization = organization
        return this
    }

    /**
     * @see OpenAIOkHttpClient.Builder.project
     */
    fun project(project: String): OpenAIOkHttpClientBuilder {
        this.project = project
        return this
    }

    /**
     * @see OpenAIOkHttpClient.Builder.timeout
     */
    fun timeout(timeout: Duration): OpenAIOkHttpClientBuilder {
        this.timeout = timeout
        return this
    }

    /**
     * @see OpenAIOkHttpClient.Builder.maxRetries
     */
    fun maxRetries(maxRetries: Int): OpenAIOkHttpClientBuilder {
        this.maxRetries = maxRetries
        return this
    }

    override fun toString(): String {
        return "OpenAIOkHttpClientBuilder(apiKey=$apiKey, azureApiKey=$azureApiKey, baseUrl=$baseUrl, organization=$organization, project=$project, timeout=$timeout, maxRetries=$maxRetries)"
    }

}

fun OpenAIOkHttpClientBuilder.toActualBuilder(): OpenAIOkHttpClient.Builder {
    return OpenAIOkHttpClient.builder().apply {
        this@toActualBuilder.apiKey?.let { apiKey(it) }
        this@toActualBuilder.azureApiKey?.let { credential(AzureApiKeyCredential.create(it)) }
        this@toActualBuilder.baseUrl?.let { baseUrl(it) }
        this@toActualBuilder.organization?.let { organization(it) }
        this@toActualBuilder.project?.let { project(it) }
        this@toActualBuilder.timeout?.let { timeout(it) }
        this@toActualBuilder.maxRetries?.let { maxRetries(it) }
    }
}
