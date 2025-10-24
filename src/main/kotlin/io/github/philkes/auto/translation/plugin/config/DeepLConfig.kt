package io.github.philkes.auto.translation.plugin.config

import com.deepl.api.TextTranslationOptions
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

abstract class DeepLConfig @Inject constructor(objects: ObjectFactory) : ProviderConfig {
    /**
     * DeepL API authentication key (see
     * [API Key for DeepL API](https://support.deepl.com/hc/en-us/articles/360020695820-API-key-for-DeepL-API))
     */
    @get:Input val authKey: Property<String> = objects.property(String::class.java)

    /**
     * DeepL specific options for the Translation API (see
     * [deepl-java#text-translation-options](https://github.com/DeepLcom/deepl-java?tab=readme-ov-file#text-translation-options))
     */
    @get:Input
    @get:Optional
    val options: Property<TextTranslationOptions> =
        objects.property(TextTranslationOptions::class.java)

    @Internal
    override fun isValid(): Boolean {
        return !authKey.orNull.isNullOrBlank()
    }

    @Internal
    override fun getConstraints(): String {
        return "'authKey' must be set"
    }
}
