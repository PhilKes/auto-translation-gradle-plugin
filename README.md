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
- provider: String (default: "deepl")
- apiKey: String? (provider API key; for DeepL set DEEPL auth key)
- targetLanguages: List<String>? (optional; if empty, languages are detected from existing src/main/res/values-<locale> folders)
- resDir: Directory (default: src/main/res)
- detectLanguagesFromProject: Boolean (default: true)

Providers
- deepl (uses com.deepl.api.DeepLClient and an API key)

Notes
- Only <string> resources are handled currently (no string-array/plurals yet).
- Placeholders like %1$s are passed through unchanged; review translations as needed.