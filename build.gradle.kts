import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.0.20"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.0"
}

group = "io.github.philkes"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    // uses commons-compress:1.12, higher version incompatible with some android projects
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("com.deepl.api:deepl-java:1.10.3")
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("io.mockk:mockk:1.13.5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

gradlePlugin {
    website = "https://github.com/PhilKes/android-translations-converter-plugin"
    vcsUrl = "https://github.com/PhilKes/android-translations-converter-plugin"
    description = "Automatically translate your strings.xml via external APIs"
    plugins {
        create("autoTranslateStrings") {
            id = "io.github.philkes.android-auto-translation"
            displayName = "Android Auto Translations"
            description = "Automatically translate your strings.xml via external APIs"
            tags = listOf("android", "translation", "ai")
            implementationClass = "io.github.philkes.android.auto.translation.AndroidAutoTranslationPlugin"
        }
    }
}