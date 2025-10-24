package io.github.philkes.auto.translation.plugin.util

import java.util.Locale

fun String.toIsoLocale(): Locale? {
    if (isBlank()) return null
    // Normalize like "en-US" → "en", "US"
    val parts = split('-', '_')
    val language = parts.getOrElse(0) { "" }
    // Android values have a "r" before the region/country ISO Code, e.g. zh-rCN
    val country = parts.getOrElse(1) { "" }.replace("r", "")
    val variant = parts.getOrElse(2) { "" }
    val locale = Locale(language, country, variant)
    return Locale.getAvailableLocales().find {
        it.language == locale.language &&
            it.country == locale.country &&
            it.variant == locale.variant
    }
}

val Any.readableClassName: String
    get() {
        return javaClass.simpleName.removeSuffix("_Decorated")
    }

private const val SYSTEM_PROPERTY_IN_TEST = "RUNNING_UNIT_TEST"

val isUnitTest: Boolean
    get() = System.getProperty(SYSTEM_PROPERTY_IN_TEST) == "true"

fun setIsUnitTest(value: Boolean) {
    System.setProperty(SYSTEM_PROPERTY_IN_TEST, "" + value)
}

val Locale.androidCode: String
    get() = "${language}${if(country.isNullOrBlank()) "" else "-r${country}"}"

val DOLLAR = "\$"
