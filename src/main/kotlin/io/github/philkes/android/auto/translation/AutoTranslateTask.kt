package io.github.philkes.android.auto.translation

import io.github.philkes.android.auto.translation.provider.DeepLTranslationProvider
import io.github.philkes.android.auto.translation.provider.TranslationProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Element

abstract class AutoTranslateTask @Inject constructor() : DefaultTask() {

    @get:Input
    abstract val provider: Property<String>

    @get:Input
    @get:Optional
    abstract val apiKey: Property<String>

    @get:Input
    @get:Optional
    abstract val sourceLanguage: Property<String>

    @get:Input
    @get:Optional
    abstract val targetLanguages: ListProperty<String>

    @get:InputDirectory
    abstract val resDir: DirectoryProperty

    @get:Input
    abstract val detectLanguagesFromProject: Property<Boolean>

    // Optional provider override for testing via secondary constructor
    private var overrideProvider: TranslationProvider? = null

    // Secondary constructor intended for tests to override provider
    constructor(translationProvider: TranslationProvider) : this() {
        this.overrideProvider = translationProvider
    }

    // Setter to override provider in tests when default constructor is used
    fun setTranslationProviderForTesting(provider: TranslationProvider) {
        this.overrideProvider = provider
    }

    init {
        description = "Auto-translate Android strings.xml files"
        group = "translations"

        // Defaults
        provider.convention("deepl")
        detectLanguagesFromProject.convention(true)
        resDir.convention(project.layout.projectDirectory.dir("src/main/res"))
        sourceLanguage.convention("en")
        targetLanguages.convention(emptyList())
    }

    @TaskAction
    fun translate() {
        val res = resDir.get().asFile
        val chosenProvider = provider.get().lowercase()
        val langs = resolveTargetLanguages(res)
        if (langs.isEmpty()) {
            logger.lifecycle("No target languages resolved. Nothing to translate.")
            return
        }
        val translationProvider: TranslationProvider? = overrideProvider ?: when (chosenProvider) {
            "deepl" -> {
                val key = apiKey.orNull
                if (key.isNullOrBlank()) {
                    logger.warn("No API key provided for DeepL. Skipping actual translation; dry-run only.")
                    null
                } else {
                    DeepLTranslationProvider(key)
                }
            }
            else -> null
        }
        if (translationProvider == null){
            logger.warn("No valid translation provider '$chosenProvider' configured. Supported: deepl. Skipping.")
            return
        }
        logger.log(LogLevel.LIFECYCLE, "AutoTranslateTask: provider=$chosenProvider, resDir=${res.absolutePath}, targetLanguages=$langs")
        val baseDir = File(res, "values")
        val baseFile = File(baseDir, "strings.xml")
        if (!baseFile.exists()) {
            logger.warn("Base strings.xml not found at ${baseFile.absolutePath}. Nothing to translate.")
            return
        }

        val baseStrings = parseStringsXml(baseFile)
        if (baseStrings.isEmpty()) {
            logger.lifecycle("No translatable <string> entries found in base strings.xml. Nothing to do.")
            return
        }

        langs.forEach { lang ->
            val targetDir = File(res, "values-$lang")
            val targetFile = File(targetDir, "strings.xml")

            val existing = if (targetFile.exists()) parseStringsXml(targetFile) else emptyMap()
            val missingKeys = baseStrings.keys.filter { it !in existing.keys }
            if (missingKeys.isEmpty()) {
                logger.lifecycle("[$lang] All strings already present (${existing.size}). Skipping.")
                return@forEach
            }

            val textsToTranslate = missingKeys.map { key -> baseStrings[key] ?: "" }
            val translated: List<String> = try {
                translationProvider.translateBatch(textsToTranslate, sourceLanguage.getOrElse("en"), lang)
            } catch (e: Exception) {
                logger.warn("[$lang] Translation failed: ${'$'}{e.message}")
                return@forEach
            }

            val additions = missingKeys.zip(translated).toMap()
            val merged = LinkedHashMap<String, String>().apply {
                // preserve base order for missing ones; for existing keep their current order when writing
                putAll(existing)
                additions.forEach { (k, v) -> this[k] = v }
            }

            // Ensure directory exists
            if (!targetDir.exists()) targetDir.mkdirs()
            writeStringsXml(targetFile, merged)
            logger.lifecycle("[$lang] Wrote ${additions.size} new translations. Total strings now: ${merged.size}.")
        }
    }

    private fun resolveTargetLanguages(res: File): List<String> {
        val explicit = targetLanguages.getOrElse(emptyList())
        if (explicit.isNotEmpty()) return explicit
        if (!detectLanguagesFromProject.get()) return emptyList()

        val locales = mutableSetOf<String>()
        if (res.exists()) {
            res.listFiles()?.forEach { dir ->
                if (dir.isDirectory && dir.name.startsWith("values-")) {
                    val qualifier = dir.name.removePrefix("values-")
                    if (qualifier.isNotBlank()) {
                        // Take the first segment as language code (e.g., "en", "de", "pt")
                        val lang = qualifier.split("-", limit = 2).first()
                        locales.add(lang)
                    }
                }
            }
        }
        return locales.sorted()
    }

    private fun parseStringsXml(file: File): Map<String, String> {
        return try {
            val dbf = DocumentBuilderFactory.newInstance()
            dbf.isNamespaceAware = true
            val builder = dbf.newDocumentBuilder()
            val doc = builder.parse(file)
            val list = doc.getElementsByTagName("string")
            val map = LinkedHashMap<String, String>()
            for (i in 0 until list.length) {
                val node = list.item(i)
                if (node is Element) {
                    val name = node.getAttribute("name")
                    val translatable = node.getAttribute("translatable")
                    if (name.isNullOrBlank()) continue
                    if (translatable.equals("false", ignoreCase = true)) continue
                    val text = node.textContent ?: ""
                    map[name] = text
                }
            }
            map
        } catch (e: Exception) {
            logger.warn("Failed to parse strings.xml at ${'$'}{file.absolutePath}: ${'$'}{e.message}")
            emptyMap()
        }
    }

    private fun writeStringsXml(file: File, entries: Map<String, String>) {
        try {
            val doc = newDocument()
            val resources = doc.createElement("resources")
            doc.appendChild(resources)
            // Write in sorted order for determinism
            entries.toSortedMap().forEach { (name, value) ->
                val el = doc.createElement("string")
                el.setAttribute("name", name)
                el.appendChild(doc.createTextNode(value))
                resources.appendChild(el)
            }

            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name())
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(DOMSource(doc), StreamResult(file))
        } catch (e: Exception) {
            logger.warn("Failed to write strings.xml at ${'$'}{file.absolutePath}: ${'$'}{e.message}")
        }
    }

    private fun newDocument(): Document {
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true
        return dbf.newDocumentBuilder().newDocument()
    }
}