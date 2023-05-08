package com.mohamedrejeb.ksoup.tokenizer

import com.mohamedrejeb.ksoup.entities.EntityDecoder
import com.mohamedrejeb.ksoup.entities.decodeDataHtml

class Tokenizer(
    private val xmlMode: Boolean = false,
    private val decodeEntities: Boolean = true,
    private val cbs: Callbacks
) {

    /** The current state the tokenizer is in. */
    private var state = State.Text
    /** The read buffer. */
    private var buffer = ""
    /** The beginning of the section that is currently being read. */
    private var sectionStart = 0
    /** The index within the buffer that we are currently looking at. */
    private var index = 0
    /** The start of the last entity. */
    private var entityStart = 0
    /** Some behavior, eg. when decoding entities, is done while we are in another state. This keeps track of the other state type. */
    private var baseState = State.Text
    /** For special parsing behavior inside of script and style tags. */
    private var isSpecial = false
    /** Indicates whether the tokenizer has been paused. */
    public var running = true
    /** The offset of the current buffer. */
    private var offset = 0

    private val entityDecoder = EntityDecoder(
        decodeTree = decodeDataHtml,
        emitCodePoint = { cp, consumed ->
            this.emitCodePoint(cp, consumed)
        }
    )

    fun reset() {
        this.state = State.Text
        this.buffer = ""
        this.sectionStart = 0
        this.index = 0
        this.baseState = State.Text
        this.currentSequence = null
        this.running = true
        this.offset = 0
    }

    fun write(chunk: String) {
        this.offset += this.buffer.length;
        this.buffer = chunk
        this.parse()
    }

    fun end() {
        if (this.running) this.finish()
    }

    fun pause() {
        this.running = false
    }

    fun resume(): void {
        this.running = true
        if (this.index < this.buffer.length + this.offset) {
            this.parse()
        }
    }

    private fun stateText(c: Int) {
        if (
            c == CharCodes.Lt ||
            (!this.decodeEntities && this.fastForwardTo(CharCodes.Lt))
        ) {
            if (this.index > this.sectionStart) {
                this.cbs.ontext(this.sectionStart, this.index)
            }
            this.state = State.BeforeTagName
            this.sectionStart = this.index
        } else if (this.decodeEntities && c == CharCodes.Amp) {
            this.startEntity()
        }
    }

    private var currentSequence: Uint8Array? = null
    private var sequenceIndex = 0
    private var stateSpecialStartSequence(c: Int) {
        val isEnd = this.sequenceIndex == this.currentSequence.length;
        val isMatch = if (isEnd) {
            // If we are at the end of the sequence, make sure the tag name has ended
            isEndOfTagSection(c)
        } else {
            // Otherwise, do a case-insensitive comparison
            (c or 0x20) == this.currentSequence[this.sequenceIndex]
        }

        if (!isMatch) {
            this.isSpecial = false
        } else if (!isEnd) {
            this.sequenceIndex++
            return
        }

        this.sequenceIndex = 0
        this.state = State.InTagName
        this.stateInTagName(c)
    }

    /** Look for an end tag. For <title> tags, also decode entities. */
    private fun stateInSpecialTag(c: Int) {
        if (this.sequenceIndex == this.currentSequence.length) {
            if (c == CharCodes.Gt || isWhitespace(c)) {
                val endOfText = this.index - this.currentSequence.length

                if (this.sectionStart < endOfText) {
                    // Spoof the index so that reported locations match up.
                    const actualIndex = this.index
                    this.index = endOfText
                    this.cbs.ontext(this.sectionStart, endOfText)
                    this.index = actualIndex
                }

                this.isSpecial = false
                this.sectionStart = endOfText + 2 // Skip over the `</`
                this.stateInClosingTagName(c)
                return // We are done; skip the rest of the function.
            }

            this.sequenceIndex = 0
        }

        if ((c or 0x20) == this.currentSequence[this.sequenceIndex]) {
            this.sequenceIndex += 1;
        } else if (this.sequenceIndex === 0) {
            if (this.currentSequence === Sequences.TitleEnd) {
                // We have to parse entities in <title> tags.
                if (this.decodeEntities && c === CharCodes.Amp) {
                    this.startEntity();
                }
            } else if (this.fastForwardTo(CharCodes.Lt)) {
                // Outside of <title> tags, we can fast-forward.
                this.sequenceIndex = 1;
            }
        } else {
            // If we see a `<`, set the sequence index to 1; useful for eg. `<</script>`.
            this.sequenceIndex = Number(c === CharCodes.Lt);
        }
    }

    /** All the states the tokenizer can be in. */
    enum class State {
        Text,
        BeforeTagName, // After <
        InTagName,
        InSelfClosingTag,
        BeforeClosingTagName,
        InClosingTagName,
        AfterClosingTagName,

        // Attributes
        BeforeAttributeName,
        InAttributeName,
        AfterAttributeName,
        BeforeAttributeValue,
        InAttributeValueDq, // "
        InAttributeValueSq, // '
        InAttributeValueNq,

        // Declarations
        BeforeDeclaration, // !
        InDeclaration,

        // Processing instructions
        InProcessingInstruction, // ?

        // Comments & CDATA
        BeforeComment,
        CDATASequence,
        InSpecialComment,
        InCommentLike,

        // Special tags
        BeforeSpecialS, // Decide if we deal with `<script` or `<style`
        SpecialStartSequence,
        InSpecialTag,

        InEntity,
    }

}