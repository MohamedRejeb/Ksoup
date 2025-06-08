package com.mohamedrejeb.ksoup.html.tokenizer

import com.mohamedrejeb.ksoup.annotation.ExperimentalKsoupApi
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser

@SubclassOptInRequired(ExperimentalKsoupApi::class)
public interface KsoupTokenizerCallbacks {

    public fun onAttribData(start: Int, endIndex: Int) {}

    public fun onAttribEntity(codepoint: Int) {}

    public fun onAttribEnd(quote: KsoupHtmlParser.QuoteType, endIndex: Int) {}

    public fun onAttribName(start: Int, endIndex: Int) {}

    public fun onCData(start: Int, endIndex: Int, offset: Int) {}

    public fun onCloseTag(start: Int, endIndex: Int) {}

    public fun onComment(start: Int, endIndex: Int, offset: Int) {}

    public fun onDeclaration(start: Int, endIndex: Int) {}

    public fun onEnd() {}

    public fun onOpenTagEnd(endIndex: Int) {}

    public fun onOpenTagName(start: Int, endIndex: Int) {}

    public fun onProcessingInstruction(start: Int, endIndex: Int) {}

    public fun onSelfClosingTag(endIndex: Int) {}

    public fun onText(start: Int, endIndex: Int) {}

    public fun onTextEntity(codepoint: Int, endIndex: Int) {}

    public object Default : KsoupTokenizerCallbacks

    @ExperimentalKsoupApi
    public class Builder: KsoupTokenizerCallbacks {

        private var callbacks: KsoupTokenizerCallbacks = KsoupTokenizerCallbacks.Default

        public fun onAttribData(block: (start: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onAttribData(start: Int, endIndex: Int) {
                    block(start, endIndex)
                }
            }
            return this
        }

        public fun onAttribEntity(block: (codepoint: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onAttribEntity(codepoint: Int) {
                    block(codepoint)
                }
            }
            return this
        }

        public fun onAttribEnd(
            block: (
                quote: KsoupHtmlParser.QuoteType,
                endIndex: Int
            ) -> Unit
        ): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onAttribEnd(
                    quote: KsoupHtmlParser.QuoteType,
                    endIndex: Int
                ) {
                    block(quote, endIndex)
                }
            }
            return this
        }

        public fun onAttribName(block: (start: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onAttribName(start: Int, endIndex: Int) {
                    block(start, endIndex)
                }
            }
            return this
        }

        public fun onCData(block: (start: Int, endIndex: Int, offset: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onCData(start: Int, endIndex: Int, offset: Int) {
                    block(start, endIndex, offset)
                }
            }
            return this
        }

        public fun onCloseTag(block: (start: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onCloseTag(start: Int, endIndex: Int) {
                    block(start, endIndex)
                }
            }
            return this
        }

        public fun onComment(block: (start: Int, endIndex: Int, offset: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onComment(start: Int, endIndex: Int, offset: Int) {
                    block(start, endIndex, offset)
                }
            }
            return this
        }

        public fun onDeclaration(block: (start: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onDeclaration(start: Int, endIndex: Int) {
                    block(start, endIndex)
                }
            }
            return this
        }

        public fun onEnd(block: () -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onEnd() {
                    block()
                }
            }
            return this
        }

        public fun onOpenTagEnd(block: (endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onOpenTagEnd(endIndex: Int) {
                    block(endIndex)
                }
            }
            return this
        }

        public fun onOpenTagName(block: (start: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onOpenTagName(start: Int, endIndex: Int) {
                    block(start, endIndex)
                }
            }
            return this
        }

        public fun onProcessingInstruction(block: (start: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onProcessingInstruction(start: Int, endIndex: Int) {
                    block(start, endIndex)
                }
            }
            return this
        }

        public fun onSelfClosingTag(block: (endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onSelfClosingTag(endIndex: Int) {
                    block(endIndex)
                }
            }
            return this
        }

        public fun onText(block: (start: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onText(start: Int, endIndex: Int) {
                    block(start, endIndex)
                }
            }
            return this
        }

        public fun onTextEntity(block: (codepoint: Int, endIndex: Int) -> Unit): KsoupTokenizerCallbacks {
            callbacks = object : KsoupTokenizerCallbacks by callbacks {
                override fun onTextEntity(codepoint: Int, endIndex: Int) {
                    block(codepoint, endIndex)
                }
            }
            return this
        }

        public fun build(): KsoupTokenizerCallbacks = callbacks

    }
}