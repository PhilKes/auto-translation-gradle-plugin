package io.github.philkes.auto.translation.plugin.config

import org.gradle.api.tasks.Internal

sealed interface ProviderConfig {
    @Internal fun isValid(): Boolean

    @Internal fun getConstraints(): String
}
