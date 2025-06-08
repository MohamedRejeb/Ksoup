package com.mohamedrejeb.ksoup.html.parser

public data class KsoupHtmlOptions(

    /**
     * Indicates whether special tags (`<script>`, `<style>`, and `<title>`) should get special treatment
     * and if "empty" tags (eg. `<br>`) can have children.  If `false`, the content of special tags
     * will be text only. For feeds and other XML content (documents that don't consist of HTML),
     * set this to `true`.
     *
     * @default false
     */
    val xmlMode: Boolean = false,

    /**
     * Decode entities within the document.
     *
     * @default true
     */
    val decodeEntities: Boolean = true,

    /**
     * If set to true, all tags will be lowercased.
     *
     * @default !xmlMode
     */
    val lowerCaseTags: Boolean = !xmlMode,

    /**
     * If set to `true`, all attribute names will be lowercased. This has noticeable impact on speed.
     *
     * @default !xmlMode
     */
    val lowerCaseAttributeNames: Boolean = !xmlMode,

    /**
     * If set to true, CDATA sections will be recognized as text even if the xmlMode option is not enabled.
     * NOTE: If xmlMode is set to `true` then CDATA sections will always be recognized as text.
     *
     * @default xmlMode
     */
    val recognizeCDATA: Boolean = xmlMode,

    /**
     * If set to `true`, self-closing tags will trigger the onclosetag event even if xmlMode is not set to `true`.
     * NOTE: If xmlMode is set to `true` then self-closing tags will always be recognized.
     *
     * @default xmlMode
     */
    val recognizeSelfClosing: Boolean = xmlMode,
) {
    public class Builder {
        private var options = KsoupHtmlOptions()

        public fun xmlMode(xmlMode: Boolean): Builder {
            options = options.copy(xmlMode = xmlMode)
            return this
        }

        public fun decodeEntities(decodeEntities: Boolean): Builder {
            options = options.copy(decodeEntities = decodeEntities)
            return this
        }

        public fun lowerCaseTags(lowerCaseTags: Boolean): Builder {
            options = options.copy(lowerCaseTags = lowerCaseTags)
            return this
        }

        public fun lowerCaseAttributeNames(lowerCaseAttributeNames: Boolean): Builder {
            options = options.copy(lowerCaseAttributeNames = lowerCaseAttributeNames)
            return this
        }

        public fun recognizeCDATA(recognizeCDATA: Boolean): Builder {
            options = options.copy(recognizeCDATA = recognizeCDATA)
            return this
        }

        public fun recognizeSelfClosing(recognizeSelfClosing: Boolean): Builder {
            options = options.copy(recognizeSelfClosing = recognizeSelfClosing)
            return this
        }

        public fun build(): KsoupHtmlOptions = options
    }

    public companion object {
        public val Default: KsoupHtmlOptions = KsoupHtmlOptions()
    }
}