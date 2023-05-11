package com.mohamedrejeb.ksoup.html.parser

public interface KsoupHtmlHandler {

    public fun onParserInit(ksoupHtmlParser: KsoupHtmlParser) {}

    /**
     * Reset the handler back to starting state
     */
    public fun onReset() {}

    /**
     * Signal that the parser is done parsing the document
     */
    public fun onEnd() {}

    public fun onError(error: Exception) {}

    public fun onCloseTag(name: String, isImplied: Boolean) {}

    public fun onOpenTagName(name: String) {}

    /**
     *
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @param quote The quotes used around the attribute. `null` if the attribute has no quotes around the value or if the attribute has no value.
     */
    public fun onAttribute(
        name: String,
        value: String,
        quote: String? = null,
    ) {}

    public fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {}

    public fun onText(text: String) {}

    public fun onComment(comment: String) {}

    public fun onCDataStart() {}

    public fun onCDataEnd() {}

    public fun onCommentEnd() {}

    public fun onProcessingInstruction(
        name: String,
        data: String,
    ) {}

    public object Default : KsoupHtmlHandler

    public class Builder {

        private var handler: KsoupHtmlHandler = Default

        public fun onParserInit(block: (ksoupHtmlParser: KsoupHtmlParser) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onParserInit(ksoupHtmlParser: KsoupHtmlParser) {
                    block(ksoupHtmlParser)
                }
            }
            return this
        }

        public fun onReset(block: () -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onReset() {
                    block()
                }
            }
            return this
        }

        public fun onEnd(block: () -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onEnd() {
                    block()
                }
            }
            return this
        }

        public fun onError(block: (error: Exception) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onError(error: Exception) {
                    block(error)
                }
            }
            return this
        }

        public fun onCloseTag(block: (name: String, isImplied: Boolean) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onCloseTag(name: String, isImplied: Boolean) {
                    block(name, isImplied)
                }
            }
            return this
        }

        public fun onOpenTagName(block: (name: String) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onOpenTagName(name: String) {
                    block(name)
                }
            }
            return this
        }

        public fun onAttribute(block: (name: String, value: String, quote: String?) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onAttribute(name: String, value: String, quote: String?) {
                    block(name, value, quote)
                }
            }
            return this
        }

        public fun onOpenTag(block: (name: String, attributes: Map<String, String>, isImplied: Boolean) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
                    block(name, attributes, isImplied)
                }
            }
            return this
        }

        public fun onText(block: (text: String) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onText(text: String) {
                    block(text)
                }
            }
            return this
        }

        public fun onComment(block: (comment: String) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onComment(comment: String) {
                    block(comment)
                }
            }
            return this
        }

        public fun onCDataStart(block: () -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onCDataStart() {
                    block()
                }
            }
            return this
        }

        public fun onCDataEnd(block: () -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onCDataEnd() {
                    block()
                }
            }
            return this
        }

        public fun onCommentEnd(block: () -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onCommentEnd() {
                    block()
                }
            }
            return this
        }

        public fun onProcessingInstruction(block: (name: String, data: String) -> Unit): Builder {
            handler = object : KsoupHtmlHandler by handler {
                override fun onProcessingInstruction(name: String, data: String) {
                    block(name, data)
                }
            }
            return this
        }

        public fun build(): KsoupHtmlHandler = handler

    }

}