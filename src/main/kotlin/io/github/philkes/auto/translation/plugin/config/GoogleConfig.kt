package io.github.philkes.auto.translation.plugin.config

import com.google.cloud.translate.TranslateOptions
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal

abstract class GoogleConfig @Inject constructor(objects: ObjectFactory) : ProviderConfig {

    /**
     * Google specific options for the Translation API.
     *
     * At least [TranslateOptions.Builder.credentials] or environment variable
     * `GOOGLE_API_KEY`([TranslateOptions.API_KEY_ENV_NAME]) has to be set. (for creating an API
     * Key, see
     * [generate-google-api-key](https://translatepress.com/docs/automatic-translation/generate-google-api-key/))
     *
     * (see
     * [Google-Cloud-Java#about-cloud-translation](https://github.com/googleapis/google-cloud-java/tree/main/java-translate#about-cloud-translation))
     */
    val options: Property<TranslateOptions.Builder> =
        objects.property(TranslateOptions.Builder::class.java)

    /**
     * Overwrite model used for translation.
     *
     * (see [com.google.cloud.translate.Translate.TranslateOption.model])
     */
    val model: Property<String> = objects.property(String::class.java)

    init {
        options.convention(TranslateOptions.getDefaultInstance().toBuilder())
    }

    @Internal
    override fun isValid(): Boolean {
        return options.isPresent
    }

    @Internal
    override fun getConstraints(): String {
        return "'options' must be set"
    }
}
