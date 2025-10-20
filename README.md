# Android Auto Translation Plugin

A Gradle plugin that helps auto-translate Android strings.xml using external providers.

Usage
- Apply the plugin in your Android module build.gradle(.kts):
  - id("io.github.philkes.android-auto-translation")
- Run the task:
  - ./gradlew autoTranslate

What it does now
- Parses src/main/res/values/strings.xml for <string> entries (skips translatable="false").
- For each target language, creates/updates src/main/res/values-<lang>/strings.xml with missing keys translated.
- Existing keys in target files are preserved; only missing ones are added.

Inputs (AutoTranslateTask)
- provider: ProviderType enum (default: DEEPL). Options: DEEPL, GOOGLE, AZURE
- apiKey: String? (provider API key)
- targetLanguages: List<String>? (optional; if empty, languages are detected from existing src/main/res/values-<locale> folders)
- resDir: Directory (default: src/main/res)
- providerConfig: provider-specific DSL block. For Azure, configure region and optional endpoint.

Providers
- DEEPL (uses com.deepl.api.DeepLClient and an API key)
- GOOGLE (uses com.google.cloud:google-cloud-translate v2 API with API key)
- AZURE (uses com.azure:azure-ai-translation-text; requires API key and region)

Notes
- Only <string> resources are handled currently (no string-array/plurals yet).
- Placeholders like %1$s are passed through unchanged; review translations as needed.