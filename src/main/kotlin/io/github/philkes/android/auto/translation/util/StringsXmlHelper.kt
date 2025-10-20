package io.github.philkes.android.auto.translation.util

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Helper for reading and writing Android strings.xml files.
 */
class StringsXmlHelper(private val logger: Logger) {

    /**
     * Parse a strings.xml file and return a map of name -> text, skipping entries with translatable="false".
     * Throws GradleException if the file cannot be parsed.
     */
    fun parse(file: File): Map<String, String> {
        try {
            val dbf = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
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
            return map
        } catch (e: Exception) {
            logger.error("Failed to parse strings.xml at ${file.absolutePath}", e)
            throw GradleException("Failed to parse strings.xml: ${file.absolutePath}. ${e.message}", e)
        }
    }

    /**
     * Write the provided entries to a strings.xml file. Overwrites the file if it exists.
     */
    fun write(file: File, entries: Map<String, String>) {
        try {
            val doc = newDocument()
            val resources = doc.createElement("resources")
            doc.appendChild(resources)
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
            file.parentFile?.mkdirs()
            transformer.transform(DOMSource(doc), StreamResult(file))
        } catch (e: Exception) {
            logger.error("Failed to write strings.xml at ${file.absolutePath}", e)
            throw GradleException("Failed to write strings.xml: ${file.absolutePath}. ${e.message}", e)
        }
    }

    private fun newDocument(): Document {
        val dbf = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        return dbf.newDocumentBuilder().newDocument()
    }
}
