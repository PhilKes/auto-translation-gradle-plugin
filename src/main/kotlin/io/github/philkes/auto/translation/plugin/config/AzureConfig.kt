package io.github.philkes.auto.translation.plugin.config

import com.azure.ai.translation.text.TextTranslationClientBuilder
import io.github.philkes.auto.translation.plugin.provider.azure.AzureTextTranslationClientBuilder
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class AzureConfig @Inject constructor(objects: ObjectFactory) : ProviderConfig {

    /**
     * Azure specific options for the Translation API.
     *
     * At least [TextTranslationClientBuilder.credential] has to be set.
     *
     * (see
     * [Azure/ai-translation-text-readme](https://learn.microsoft.com/en-us/java/api/overview/azure/ai-translation-text-readme?view=azure-java-stable#authentication))
     */
    @get:Input
    val options: Property<AzureTextTranslationClientBuilder> =
        objects.property(AzureTextTranslationClientBuilder::class.java)

    @Internal
    override fun isValid(): Boolean {
        return options.isPresent
    }

    @Internal
    override fun getConstraints(): String {
        return "'options' must be set"
    }
}
