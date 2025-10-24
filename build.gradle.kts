import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.0.20"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.0"
    id("com.ncorti.ktfmt.gradle") version "0.24.0"
}

group = "io.github.philkes"

version = "1.0.0"

repositories { mavenCentral() }

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    // uses commons-compress:1.12, higher version incompatible with some android projects
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("com.deepl.api:deepl-java:1.10.3")
    implementation("com.google.cloud:google-cloud-translate:2.77.0")
    implementation("com.azure:azure-ai-translation-text:1.1.6")
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("io.mockk:mockk:1.13.5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test { useJUnitPlatform() }

kotlin {
    jvmToolchain(8)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
}

java { withSourcesJar() }

ktfmt { kotlinLangStyle() }

gradlePlugin {
    website = "https://github.com/PhilKes/auto-translation-gradle-plugin"
    vcsUrl = "https://github.com/PhilKes/auto-translation-gradle-plugin"
    description = "Automatically translate your strings.xml via external APIs"
    plugins {
        create("autoTranslateStrings") {
            id = "io.github.philkes.auto-translation"
            displayName = "Auto Translations"
            description =
                "Automatically translate your Android project via external Translation providers"
            tags =
                listOf("android", "translation", "ai", "google", "azure", "deepl", "libretranslate")
            implementationClass = "io.github.philkes.auto.translation.plugin.AutoTranslationPlugin"
        }
    }
}
