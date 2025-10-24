package io.github.philkes.auto.translation.plugin.config

import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ChatModel
import io.github.philkes.auto.translation.plugin.provider.OpenAITranslationService
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

/** Configuration for OpenAI translation provider. */
abstract class OpenAIConfig @Inject constructor(objects: ObjectFactory) : ProviderConfig {

    /**
     * OpenAI specific options for the Translation completions.
     *
     * At least [OpenAIOkHttpClient.Builder.apiKey] or [OpenAIOkHttpClient.Builder.credential] has
     * to be set.
     *
     * (see
     * [OpenAI/openai-java](https://github.com/openai/openai-java?tab=readme-ov-file#microsoft-azure))
     */
    @get:Input
    val options: Property<OpenAIOkHttpClient.Builder> =
        objects.property(OpenAIOkHttpClient.Builder::class.java)

    /**
     * Optional overwrite model to use.
     *
     * Defaults: `gpt-4o-mini` ([ChatModel.GPT_4O_MINI])
     */
    @get:Input @get:Optional val model: Property<String> = objects.property(String::class.java)

    /**
     * Optional overwrite the used system message.
     *
     * Defaults to: [OpenAITranslationService.DEFAULT_SYSTEM_MESSAGE]
     */
    @get:Input
    @get:Optional
    val systemMessage: Property<String> = objects.property(String::class.java)

    init {
        model.convention(ChatModel.GPT_4O_MINI.toString())
        systemMessage.convention(OpenAITranslationService.DEFAULT_SYSTEM_MESSAGE)
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
