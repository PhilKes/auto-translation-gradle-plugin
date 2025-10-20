package io.github.philkes.android.auto.translation.provider

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory

/**
 * Generic provider configuration container to support provider-specific options via DSL.
 *
 * Usage in Gradle Kotlin DSL:
 * autoTranslate {
 *   provider = TranslationProvider.AZURE
 *   apiKey = "..."
 *   providerConfig {
 *     azure {
 *       region.set("westeurope")
 *       // endpoint is optional; defaults to the public Translator endpoint
 *       // endpoint.set("https://api.cognitive.microsofttranslator.com")
 *     }
 *   }
 * }
 */
open class ProviderConfig @Inject constructor(objects: ObjectFactory) {

    @get:Nested
    val azure: AzureConfig = objects.newInstance(AzureConfig::class.java)

    fun azure(configure: AzureConfig.() -> Unit) {
        azure.configure()
    }
}

open class AzureConfig @Inject constructor(objects: ObjectFactory) {
    /** Azure region, e.g. "westeurope", "eastus". Required for Azure provider. */
    @get:Input
    @get:Optional
    val region: Property<String> = objects.property(String::class.java)

    /** Optional custom endpoint; defaults to public Translator endpoint if not set. */
    @get:Input
    @get:Optional
    val endpoint: Property<String> = objects.property(String::class.java)
}
