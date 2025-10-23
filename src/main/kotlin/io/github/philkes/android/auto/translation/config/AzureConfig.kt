package io.github.philkes.android.auto.translation.config

import com.azure.ai.translation.text.TextTranslationClientBuilder
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
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
    val options: Property<TextTranslationClientBuilder> =
        objects.property(TextTranslationClientBuilder::class.java)

    @Internal
    override fun isValid(): Boolean {
        return options.isPresent
    }

    @Internal
    override fun getConstraints(): String {
        return "'options' must be set"
    }
}
