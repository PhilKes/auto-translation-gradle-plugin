# Android Auto Translation Plugin
<a href="https://plugins.gradle.org/plugin/io.github.philkes.android-auto-translation"><img alt="Gradle Plugin Portal Version" src="https://img.shields.io/gradle-plugin-portal/v/io.github.philkes.android-auto-translation"></a>


Plug'n'Play gradle plugin for your Android projects to automatically translate your `strings.xml` as well as fastlane documentation into any language using external translation services like DeepL, Azure or Google

## Features

* Evaluate missing translations for any existing or new language
* Automatically translate the missing translations via configured external Translation Provider
* Supports [Android quantity strings (plurals)](https://developer.android.com/guide/topics/resources/string-resource#Plurals)
* Correctly escapes/unescapes [special formatting characters](https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes) + HTML tags in `strings.xml`
* Auto-Sorts the translations by their "name" attribute
* Supports translating fastlane documentation text files

### Supported APIs
- [Google-Cloud-Translate](https://cloud.google.com/translate) (Used client: [google-cloud-java/java-translate](https://github.com/googleapis/google-cloud-java/tree/main/java-translate))
- [Azure-AI-Text-Translation](https://azure.microsoft.com/en-us/products/ai-services/ai-translator) (Used client: [azure-sdk-for-java/azure-ai-translation-text](https://github.com/Azure/azure-sdk-for-java/tree/azure-ai-translation-text_1.1.6/sdk/translation/azure-ai-translation-text/))
- [DeepL](https://www.deepl.com/en/pro-api) (Used client: [DeepLcom/deepl-java](https://github.com/DeepLcom/deepl-java))

## Setup

In `build.gradle.kts`:
```groovy
plugins {
    id("io.github.philkes.android-auto-translation") version "1.0.0"
}

// Minimal configuration for e.g. DeepL for 2 languages:
androidAutoTranslate {
    targetLanguages = setOf("de-DE", "fr-FR")
    provider = deepL {
        authKey = "YOUR AUTH KEY"
    }
}
```
Run:
```shell
./gradlew androidAutoTranslate
```

### Configuration Options

Shown values are either the default values or placeholders 
```kotlin
androidAutoTranslate {
    // (Optional) Language of the 'values/strings.xml' texts
    sourceLanguage = "en"
    
    // (Optional) Languages that should be translated into (ISO codes)
    // By default target languages are evaluated from which 'values-{targetLanguage}' folders exist
    // (non-language values folders like `values-night` or `values-v31` are ignored)
    // If a `values-{targetLanguage}` does not yet exist, the plugin will create it
    targetLanguages = setOf(...)
    
    // (Optional) Path to the folder containing the 'values' subfolders
    resDirectory = layout.projectDirectory.dir("src/main/res")
    
    // CHOOSE ONE OF THE FOLLOWING PROVIDERS:
    
    // DeepL
    provider = deepL {
        // Authentication Key for DeepL
        authKey = "YOUR API KEY"
        
        // (Optional) Overwrite DeepL specific settings for the translations
        // See https://github.com/DeepLcom/deepl-java?tab=readme-ov-file#text-translation-options
        options = TextTranslationOptions()
    }
    
    // Google
    provider = google {
        // Options builder directly from Google Client library, at least credentials have to be set
        options = TranslateOptions.newBuilder()
                // Google offers many different authentication methods, e.g. api key:
                .setCredentials(ApiKeyCredentials.create("API KEY"))
        
        // (Optional) Overwrite model to use for translations
        model = "base"
    }   
    
    // Azure
    provider = azure {
        // Options builder directly from Azure Client library, at least credentials have to be set
        options = TextTranslationClientBuilder()
                // Azure offers different authentication methods, e.g. api key:
                .credential(AzureKeyCredential("YOUR API KEY"))
    }
}
```

#### Plurals

Android supports [quantity strings (plurals)](https://developer.android.com/guide/topics/resources/string-resource#Plurals).
To support these plurals, for every `<plurals>` in the `strings.xml` there are multiple `<item>` for all the supported quantities.
For every `<item>` in the source `strings.xml` file the translations will be requested separately.


#### Execute on build automatically

In `build.gradle`:
```groovy
preBuild.dependsOn androidAutoTranslate
```

#### Add as pre-commit hook

If you want `androidAutoTranslate` to be automatically executed before any git commit:
1. Copy [pre-commit folder](./pre-commit) to the root of your project
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
3. Whenever you create a git commit and there are changes in the `{resDirectory}/values/strings.xml`, the translations will be kept up-to-date
