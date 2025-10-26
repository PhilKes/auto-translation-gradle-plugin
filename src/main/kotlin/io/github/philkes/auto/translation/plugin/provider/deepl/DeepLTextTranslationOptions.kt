package io.github.philkes.auto.translation.plugin.provider.deepl

import com.deepl.api.Formality
import com.deepl.api.IGlossary
import com.deepl.api.SentenceSplittingMode
import com.deepl.api.TextTranslationOptions
import java.io.Serializable

/**
 * A builder for creating a new instance of the OpenAIOkHttpClient type.
 *
 * Copy of [TextTranslationOptions] to make it serializable
 */
class DeepLTextTranslationOptions : Serializable {
    internal var formality: Formality? = null
    internal var glossaryId: String? = null
    internal var sentenceSplittingMode: SentenceSplittingMode? = null
    internal var preserveFormatting = false
    internal var context: String? = null
    internal var tagHandling: String? = null
    internal var modelType: String? = null
    internal var outlineDetection = true
    internal var ignoreTags: Iterable<String?>? = null
    internal var nonSplittingTags: Iterable<String?>? = null
    internal var splittingTags: Iterable<String?>? = null

    /** @see com.deepl.api.TextTranslationOptions.setFormality */
    fun setFormality(formality: Formality?): DeepLTextTranslationOptions {
        this.formality = formality
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setGlossaryId */
    fun setGlossaryId(glossaryId: String?): DeepLTextTranslationOptions {
        this.glossaryId = glossaryId
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setGlossary */
    fun setGlossary(glossary: IGlossary): DeepLTextTranslationOptions {
        return setGlossary(glossary.getGlossaryId())
    }

    /** @see com.deepl.api.TextTranslationOptions.setGlossaryId */
    fun setGlossary(glossaryId: String?): DeepLTextTranslationOptions {
        this.glossaryId = glossaryId
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setContext */
    fun setContext(context: String?): DeepLTextTranslationOptions {
        this.context = context
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setSentenceSplittingMode */
    fun setSentenceSplittingMode(
        sentenceSplittingMode: SentenceSplittingMode?
    ): DeepLTextTranslationOptions {
        this.sentenceSplittingMode = sentenceSplittingMode
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setPreserveFormatting */
    fun setPreserveFormatting(preserveFormatting: Boolean): DeepLTextTranslationOptions {
        this.preserveFormatting = preserveFormatting
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setTagHandling */
    fun setTagHandling(tagHandling: String?): DeepLTextTranslationOptions {
        this.tagHandling = tagHandling
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setModelType */
    fun setModelType(modelType: String?): DeepLTextTranslationOptions {
        this.modelType = modelType
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setOutlineDetection */
    fun setOutlineDetection(outlineDetection: Boolean): DeepLTextTranslationOptions {
        this.outlineDetection = outlineDetection
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setIgnoreTags */
    fun setIgnoreTags(ignoreTags: Iterable<String?>?): DeepLTextTranslationOptions {
        this.ignoreTags = ignoreTags
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setNonSplittingTags */
    fun setNonSplittingTags(nonSplittingTags: Iterable<String?>?): DeepLTextTranslationOptions {
        this.nonSplittingTags = nonSplittingTags
        return this
    }

    /** @see com.deepl.api.TextTranslationOptions.setSplittingTags */
    fun setSplittingTags(splittingTags: Iterable<String?>?): DeepLTextTranslationOptions {
        this.splittingTags = splittingTags
        return this
    }

    override fun toString(): String {
        return "DeepLTextTranslationOptions(formality=$formality, glossaryId=$glossaryId, sentenceSplittingMode=$sentenceSplittingMode, preserveFormatting=$preserveFormatting, context=$context, tagHandling=$tagHandling, modelType=$modelType, outlineDetection=$outlineDetection, ignoreTags=$ignoreTags, nonSplittingTags=$nonSplittingTags, splittingTags=$splittingTags)"
    }
}

fun DeepLTextTranslationOptions.toActualBuilder(): TextTranslationOptions {
    return TextTranslationOptions().apply {
        this@toActualBuilder.formality?.let { setFormality(it) }
        this@toActualBuilder.glossaryId?.let { setGlossaryId(it) }
        this@toActualBuilder.sentenceSplittingMode?.let { setSentenceSplittingMode(it) }
        setPreserveFormatting(this@toActualBuilder.preserveFormatting)
        this@toActualBuilder.context?.let { setContext(it) }
        this@toActualBuilder.tagHandling?.let { setTagHandling(it) }
        this@toActualBuilder.modelType?.let { setModelType(it) }
        setOutlineDetection(this@toActualBuilder.outlineDetection)
        this@toActualBuilder.ignoreTags?.let { setIgnoreTags(it) }
        this@toActualBuilder.nonSplittingTags?.let { setNonSplittingTags(it) }
        this@toActualBuilder.splittingTags?.let { setSplittingTags(it) }
    }
}
