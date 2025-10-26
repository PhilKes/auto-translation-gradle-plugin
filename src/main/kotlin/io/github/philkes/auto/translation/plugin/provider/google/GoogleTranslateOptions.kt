package io.github.philkes.auto.translation.plugin.provider.google

import com.google.auth.Credentials
import com.google.cloud.translate.TranslateOptions
import java.io.Serializable

/**
 * A builder for creating a new instance of the TranslateOptions type.
 *
 * Copy of [com.google.cloud.translate.TranslateOptions.Builder] with the most important properties
 * for Translation API usage to make it serializable. Complex/infra settings like apiTracerFactory,
 * transport/channel providers, clocks, header providers etc. are intentionally omitted.
 */
class GoogleTranslateOptions : Serializable {
    /** OAuth credentials. Prefer using [serviceAccountJson] or [serviceAccountJsonFile]. */
    var credentials: Credentials? = null

    /** Custom service host, e.g. "https://translation.googleapis.com" (rarely needed). */
    var host: String? = null

    /** GCP project id to use. */
    var projectId: String? = null

    /** Quota/billing project id to attribute request costs. */
    var quotaProjectId: String? = null

    var clientLibToken: String? = null

    /** @see [TranslateOptions.Builder.setCredentials] */
    fun setCredentials(credentials: Credentials) = apply { this.credentials = credentials }

    /** @see [TranslateOptions.Builder.setHost] */
    fun setHost(host: String) = apply { this.host = host }

    /** @see [TranslateOptions.Builder.setProjectId] */
    fun setProjectId(projectId: String) = apply { this.projectId = projectId }

    /** @see [TranslateOptions.Builder.setQuotaProjectId] */
    fun setQuotaProjectId(quotaProjectId: String) = apply { this.quotaProjectId = quotaProjectId }

    /** @see [TranslateOptions.Builder.setClientLibToken] */
    fun setClientLibToken(clientLibToken: String) = apply { this.clientLibToken = clientLibToken }

    override fun toString(): String {
        return "GoogleTranslateOptions(credentials=$credentials, host=$host, projectId=$projectId, quotaProjectId=$quotaProjectId, clientLibToken=$clientLibToken)"
    }
}

fun GoogleTranslateOptions.toActualBuilder(): TranslateOptions.Builder {
    val builder = TranslateOptions.newBuilder()
    credentials?.let { builder.setCredentials(it) }
    host?.let { builder.setHost(it) }
    projectId?.let { builder.setProjectId(it) }
    quotaProjectId?.let { builder.setQuotaProjectId(it) }
    clientLibToken?.let { builder.setClientLibToken(it) }
    return builder
}
