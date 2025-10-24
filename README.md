# Auto Translation Plugin
<a href="https://plugins.gradle.org/plugin/io.github.philkes.auto-translation"><img alt="Gradle Plugin Portal Version" src="https://img.shields.io/gradle-plugin-portal/v/io.github.philkes.auto-translation"></a>


Gradle plugin to automatically translate your Android project (`strings.xml` + Fastlane metadata) into any language using external services like [DeepL](https://www.deepl.com/en/pro-api), [Google Cloud Translation](https://cloud.google.com/translate), [Azure AI Translator](https://azure.microsoft.com/en-us/products/ai-services/ai-translator), [LibreTranslate](https://libretranslate.com/), or [OpenAI](https://platform.openai.com/).

## Features

- Detect missing translations for any existing or new language your project's `strings.xml` files
- Automatically translate missing entries via configured Translation Provider
- Support for [Android quantity strings (plurals)](https://developer.android.com/guide/topics/resources/string-resource#Plurals)
- Correctly preserve [special formatting characters](https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes) and HTML tags in `strings.xml`
- Auto-sorts translations by their `name` attribute
- Translate Fastlane metadata text files

### Supported Translation Providers
- [Google Cloud Translation](https://cloud.google.com/translate) (Client: [google-cloud-java/java-translate](https://github.com/googleapis/google-cloud-java/tree/main/java-translate))
- [Azure AI Translator](https://azure.microsoft.com/en-us/products/ai-services/ai-translator) (Client: [azure-ai-translation-text](https://github.com/Azure/azure-sdk-for-java/tree/azure-ai-translation-text_1.1.6/sdk/translation/azure-ai-translation-text/))
- [DeepL](https://www.deepl.com/en/pro-api) (Client: [DeepLcom/deepl-java](https://github.com/DeepLcom/deepl-java))
- [LibreTranslate](https://libretranslate.com/) (Self-hosted or public instances)
- [OpenAI](https://platform.openai.com/) (Via Completions API, see [OpenAI](#openai) for more details, Client: [openai-java](https://github.com/openai/openai-java))

## Setup

In `build.gradle.kts`:
```kotlin
plugins {
    id("io.github.philkes.auto-translation") version "1.0.0"
}

// Minimal example: translate strings.xml for all present language folders using DeepL
autoTranslate {
    provider = deepL {
        authKey = "YOUR_AUTH_KEY"
        // Alternatively get it via an environment variable:
        // authKey = System.getenv("DEEPL_API_KEY")
    }
}
```

Run:
```shell
./gradlew autoTranslate
```

## Configuration

Shown values are defaults or placeholders.
```kotlin
autoTranslate {
    // Language of the source strings (values/strings.xml)
    // Default: "en-US"
    sourceLanguage = "en-US"

    // Explicit target languages (ISO codes like "de", "fr", "pt-BR").
    // If omitted or empty, targets are auto-detected from existing values-* folders.
    targetLanguages = listOf(/* ... */)

    // When auto-detecting targets, you can exclude some languages
    excludeLanguages = listOf(/* e.g. "fr", "es" */)

    // Configure strings.xml translation
    translateStringsXml {
        enabled = true // default
        // Path to the folder containing the `values/strings.xml` and `values-{targetLanguage}` folders.
        resDirectory = project.layout.projectDirectory.dir("src/main/res") // default
    }

    // Configure Fastlane metadata translation
    translateFastlane {
        enabled = false // default
        // Path to fastlane metadata root folder, containing `{targetLanguage` folders
        // with description, changelogs txt files
        metadataDirectory = project.layout.projectDirectory.dir("fastlane/metadata/android") // default
        // Language of the source fastlane documentation
        // Defaults to autoTranslate.sourceLanguage
        sourceLanguage = "en-US"
        // Explicit target languages for Fastlane
        // Defaults to autoTranslate.targetLanguages
        targetLanguages = setOf("de-DE")
    }
    
    provider = // Choose ONE of the providers from below section "Providers"
}
```

### Providers

#### DeepL
```kotlin
provider = deepL {
    // Authentication Key for DeepL API
    authKey = "YOUR_API_KEY"
    // (Optional) Overwrite DeepL specific settings for the translations
    // See https://github.com/DeepLcom/deepl-java?tab=readme-ov-file#text-translation-options
    options = TextTranslationOptions()
        .setPreserveFormatting(true)
}
```

#### Google
```kotlin
provider = google {
    // Configure Google TranslateOptions, must set at least credential
    options = TranslateOptions.newBuilder() 
                // Google offers many different authentication methods, e.g. api key:
                .setCredentials(ApiKeyCredentials.create("API_KEY"))
                // E.g. set Google Project Id for quota and billing purposes
                .setQuotaProjectId("XYZ")
    // (Optional) Specify model (e.g., "nmt" or "base")
    model = "base" // default
}
```

#### Azure
```kotlin

provider = azure {
    // Configure Azure TextTranslationClientBuilder, must set at least credential
    options = 
        TextTranslationClientBuilder() 
            // Azure offers different authentication methods, e.g. api key:
            .credential(AzureKeyCredential("YOUR_API_KEY"))
            // (Optional) E.g. set Azure Resource Id used to authorize requests
            .resourceId("XYZ")
}
```

#### LibreTranslate
```kotlin
provider = libreTranslate {
    // (Optional) Base URL should be the server root (the plugin uses the /translate endpoint)
    // Use your own instance or the public one
    baseUrl = "https://libretranslate.com" // default
    // (Optional) API key if your instance requires it
    apiKey = "YOUR_API_KEY"
}
```

#### OpenAI

Open AI does not offer a direct translation API like the other providers.

Instead, we leverage [System-Messages](https://platform.openai.com/docs/guides/prompt-engineering) and [Structured-Outputs](https://platform.openai.com/docs/guides/structured-outputs) to get translated texts from [OpenAI's completion API](https://platform.openai.com/docs/api-reference/completions).
Since there are other providers that are OpenAI compatible, you could also use this e.g. with [Ollama](https://ollama.com/blog/openai-compatibility)
```kotlin
provider = openAI {
    // Configure OpenAI OpenAIOkHttpClient, must set at least apiKey or credential
    options = OpenAIOkHttpClient.builder()
        // OpenAI offers to either the API key:
        .apiKey("YOUR_API_KEY")
        // Or set another Credential (similiar to Azure):
        .credential(BearerTokenCredential("YOUR TOKEN"))
        // (Optional) E.g. change the base url, any OpenAi-Compatible API should work
        .baseUrl("https://api.openai.com")

    // (Optional) overwrite used model
    model = "gpt-4o-mini" // default

    // (Optional) overwrite the system message used to guide the assistant
    systemMessage = """
            You're a professional translator for software projects, especially Android apps.
            You are given text in a specified source language, and should translate it in the most suitable way to the specified target language.
            The given texts can contain XML/HTML tags, as well as special string formatting placeholders like '%1$s',
             do not translate them and keep them at the position they were originally.
            The input is a JSON object that contains:
             - the texts' source language ('srcLang'),
             - the desired target languages ('targetLangs'),
             - the list of texts that should be translated.
        """ // default
}
```

### Fastlane specifics

- The Fastlane metadata root contains locale-named subfolders (e.g., `en-US`, `de-DE`).
- All `.txt` files inside these folders (recursively) are translated.
- The entire content of each `.txt` file is sent as a single string (no line splitting).
- When targets are autodetected, you can exclude some via `excludeLanguages` on the main task.

### Plurals

Android supports [quantity strings (plurals)](https://developer.android.com/guide/topics/resources/string-resource#Plurals).
For every `<item>` quantity in the sourceLanguage's `strings.xml`, a translation is requested and then written as a single `<plurals>` block with ordered quantities.

### Run on build (optional)

If you want the `autoTranslate` to run automatically on every build, in `build.gradle.kts`:
```kotlin
preBuild.dependsOn("autoTranslate")
```

### Pre-commit hook (optional)

If you want `autoTranslate` to run automatically before any git commit:
1. Copy the [pre-commit folder](./pre-commit) to the root of your project
2. In `build.gradle`:
    ```groovy
    tasks.register('installLocalGitHooks', Copy) {
        def scriptsDir = new File(rootProject.rootDir, 'scripts/')
        def hooksDir = new File(rootProject.rootDir, '.git/hooks')
        from(scriptsDir) {
            include 'pre-commit', 'pre-commit.bat'
        }
        into { hooksDir }
        inputs.files(file("${scriptsDir}/pre-commit"), file("${scriptsDir}/pre-commit.bat"))
        outputs.dir(hooksDir)
        fileMode 0775
    }
    preBuild.dependsOn installLocalGitHooks
    ```
3. Whenever you create a git commit and there are changes in `src/main/res/values/strings.xml`, translations will be kept up to date.
