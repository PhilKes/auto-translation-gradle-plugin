package io.github.philkes.auto.translation.plugin.config

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

abstract class LibreTranslateConfig @Inject constructor(objects: ObjectFactory) : ProviderConfig {

    /**
     * LibreTranslate API base url
     *
     * Defaults to: https://libretranslate.com
     */
    @get:Input @get:Optional val baseUrl: Property<String> = objects.property(String::class.java)

    /**
     * Optionally set LibreTranslate API key (see
     * [LibreTranslate API Key](https://docs.libretranslate.com/guides/manage_api_keys/))
     */
    @get:Input @get:Optional val apiKey: Property<String> = objects.property(String::class.java)

    init {
        baseUrl.convention("https://libretranslate.com")
    }

    @Internal
    override fun isValid(): Boolean {
        return true
    }

    @Internal
    override fun getConstraints(): String {
        return ""
    }
}
