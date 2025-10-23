package io.github.philkes.android.auto.translation.config

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class AutoTranslationExtension(private val objects: ObjectFactory) {

    /**
     * Language of the source strings (`src/main/res/values/strings.xml`). Defaults to: `en`
     * (English)
     */
    val sourceLanguage = objects.property<String>().convention("en")

    /**
     * Language ISO-Codes (from `values-{iso-code}` folder names) to translate. By defaults detects
     * all available langauges from the `src/main/res` folder.
     */
    val targetLanguages = objects.listProperty<String>().convention(emptyList())

    /**
     * Path to the folder containing the `values/strings.xml` and `values-{iso-code}` folders.
     * Defaults to `${projectDir}/src/main/res`
     */
    val resDirectory = objects.directoryProperty()

    /** Specify which translation provider to use and set it's options. */
    val provider: Property<ProviderConfig> = objects.property(ProviderConfig::class.java)

    /**
     * Optionally overwrite mapping from `values-{targetLanguage}` to language code used by the API.
     *
     * For example, if you have a `values-xyz` folder which should have `strings.xml` with `de-DE`
     * translations in it, you can set this to: `mapOf("xyz" to "de-DE")`
     */
    val languageCodeOverwrites: MapProperty<String, String> =
        objects.mapProperty(String::class.java, String::class.java)

    /** Create a [DeepLConfig] to use DeepL's translation api. */
    fun deepL(action: DeepLConfig.() -> Unit): DeepLConfig {
        val cfg = objects.newInstance(DeepLConfig::class.java)
        cfg.action()
        return cfg
    }

    /** Create a [AzureConfig] to use Azure's translation api. */
    fun azure(action: AzureConfig.() -> Unit): AzureConfig {
        val cfg = objects.newInstance(AzureConfig::class.java)
        cfg.action()
        return cfg
    }

    /** Create a [GoogleConfig] to use Google's translation api. */
    fun google(action: GoogleConfig.() -> Unit): GoogleConfig {
        val cfg = objects.newInstance(GoogleConfig::class.java)
        cfg.action()
        return cfg
    }
}
