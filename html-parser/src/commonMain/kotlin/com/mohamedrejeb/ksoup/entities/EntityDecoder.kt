package com.mohamedrejeb.ksoup.entities

import com.mohamedrejeb.ksoup.utils.pow
import kotlin.math.max

class EntityDecoder @OptIn(ExperimentalUnsignedTypes::class) constructor(
    private val decodeTree: UShortArray,
    private val emitCodePoint: (cp: Int, consumed: Int) -> Unit,
    private val errors: EntityErrorProducer? = null,
) {
    enum class CharCodes(val code: Int) {
        NUM(35), // "#"
        SEMI(59), // ""
        EQUALS(61), // "="
        ZERO(48), // "0"
        NINE(57), // "9"
        LOWER_A(97), // "a"
        LOWER_F(102), // "f"
        LOWER_X(120), // "x"
        LOWER_Z(122), // "z"
        UPPER_A(65), // "A"
        UPPER_F(70), // "F"
        UPPER_Z(90), // "Z"
    }

    enum class BinTrieFlags(val flag: Int) {
        VALUE_LENGTH(0b1100_0000_0000_0000),
        BRANCH_LENGTH(0b0011_1111_1000_0000),
        JUMP_TABLE(0b0000_0000_0111_1111),
    }

    enum class EntityDecoderState {
        EntityStart,
        NumericStart,
        NumericDecimal,
        NumericHex,
        NamedEntity,
    }

    enum class DecodingMode {
        /** Entities in text nodes that can end with any character. */
        Legacy,
        /** Only allow entities terminated with a semicolon. */
        Strict,
        /** Entities in attributes have limitations on ending characters. */
        Attribute,
    }

    /**
     * Producers for character reference errors as defined in the HTML spec.
     */
    interface EntityErrorProducer {
        fun missingSemicolonAfterCharacterReference()
        fun absenceOfDigitsInNumericCharacterReference(consumedCharacters: Int)
        fun validateNumericCharacterReference(code: Int)
    }

    /** The current state of the decoder. */
    private var state = EntityDecoderState.EntityStart
    /** Characters that were consumed while parsing an entity. */
    private var consumed = 1
    /**
     * The result of the entity.
     *
     * Either the result index of a numeric entity, or the codepoint of a
     * numeric entity.
     */
    private var result = 0

    /** The current index in the decode tree. */
    private var treeIndex = 0
    /** The number of characters that were consumed in excess. */
    private var excess = 1
    /** The mode in which the decoder is operating. */
    private var decodeMode = DecodingMode.Strict

    /** Resets the instance to make it reusable. */
    fun startEntity(decodeMode: DecodingMode) {
        this.decodeMode = decodeMode
        this.state = EntityDecoderState.EntityStart
        this.result = 0
        this.treeIndex = 0
        this.excess = 1
        this.consumed = 1
    }

    /**
     * Write an entity to the decoder. This can be called multiple times with partial entities.
     * If the entity is incomplete, the decoder will return -1.
     *
     * Mirrors the implementation of `getDecoder`, but with the ability to stop decoding if the
     * entity is incomplete, and resume when the next string is written.
     *
     * @param string The string containing the entity (or a continuation of the entity).
     * @param offset The offset at which the entity begins. Should be 0 if this is not the first call.
     * @returns The number of characters that were consumed, or -1 if the entity is incomplete.
     */
    fun write(str: String, offset: Int): Int {
        when (this.state) {
            EntityDecoderState.EntityStart -> {
                if (str[offset].code == CharCodes.NUM.code) {
                    this.state = EntityDecoderState.NumericStart
                    this.consumed += 1
                    return this.stateNumericStart(str, offset + 1)
                }
                this.state = EntityDecoderState.NamedEntity
                return this.stateNamedEntity(str, offset)
            }

            EntityDecoderState.NumericStart -> {
                return this.stateNumericStart(str, offset)
            }

            EntityDecoderState.NumericDecimal -> {
                return this.stateNumericDecimal(str, offset)
            }

            EntityDecoderState.NumericHex -> {
                return this.stateNumericHex(str, offset)
            }

            EntityDecoderState.NamedEntity -> {
                return this.stateNamedEntity(str, offset)
            }
        }
    }

    /**
     * Switches between the numeric decimal and hexadecimal states.
     *
     * Equivalent to the `Numeric character reference state` in the HTML spec.
     *
     * @param str The string containing the entity (or a continuation of the entity).
     * @param offset The current offset.
     * @returns The number of characters that were consumed, or -1 if the entity is incomplete.
     */
    private fun stateNumericStart(str: String, offset: Int): Int {
        if (offset >= str.length) {
            return -1
        }

        if ((str[offset].code or TO_LOWER_BIT) == CharCodes.LOWER_X.code) {
            this.state = EntityDecoderState.NumericHex
            this.consumed += 1
            return this.stateNumericHex(str, offset + 1)
        }

        this.state = EntityDecoderState.NumericDecimal
        return this.stateNumericDecimal(str, offset)
    }

    private fun addToNumericResult(
        str: String,
        start: Int,
        end: Int,
        base: Int
    ) {
        if (start != end) {
            val digitCount = end - start
            
            this.result = this.result * base.pow(digitCount) + (str.substring(start, digitCount).toIntOrNull(base) ?: 0)
            this.consumed += digitCount
        }
    }

    /**
     * Parses a hexadecimal numeric entity.
     *
     * Equivalent to the `Hexademical character reference state` in the HTML spec.
     *
     * @param str The string containing the entity (or a continuation of the entity).
     * @param offset The current offset.
     * @returns The number of characters that were consumed, or -1 if the entity is incomplete.
     */
    private fun stateNumericHex(str: String, offset: Int): Int {
        var offset = offset
        val startIdx = offset

        while (offset < str.length) {
            val char = str[offset].code
            if (isNumber(char) || isHexadecimalCharacter(char)) {
                offset += 1
            } else {
                this.addToNumericResult(str, startIdx, offset, 16)
                return this.emitNumericEntity(char, 3)
            }
        }

        this.addToNumericResult(str, startIdx, offset, 16)

        return -1
    }

    /**
     * Parses a decimal numeric entity.
     *
     * Equivalent to the `Decimal character reference state` in the HTML spec.
     *
     * @param str The string containing the entity (or a continuation of the entity).
     * @param offset The current offset.
     * @returns The number of characters that were consumed, or -1 if the entity is incomplete.
     */
    private fun stateNumericDecimal(str: String, offset: Int): Int {
        var offset = offset
        val startIdx = offset

        while (offset < str.length) {
            val char = str[offset].code
            if (isNumber(char)) {
                offset += 1
            } else {
                this.addToNumericResult(str, startIdx, offset, 10)
                return this.emitNumericEntity(char, 2)
            }
        }

        this.addToNumericResult(str, startIdx, offset, 10)

        return -1
    }

    /**
     * Validate and emit a numeric entity.
     *
     * Implements the logic from the `Hexademical character reference start
     * state` and `Numeric character reference end state` in the HTML spec.
     *
     * @param lastCp The last code point of the entity. Used to see if the
     *               entity was terminated with a semicolon.
     * @param expectedLength The minimum number of characters that should be
     *                       consumed. Used to validate that at least one digit
     *                       was consumed.
     * @returns The number of characters that were consumed.
     */
    private fun emitNumericEntity(lastCp: Int, expectedLength: Int): Int {
        // Ensure we consumed at least one digit.
        if (this.consumed <= expectedLength) {
            this.errors?.absenceOfDigitsInNumericCharacterReference(
                this.consumed
            )
            return 0
        }

        // Figure out if this is a legit end of the entity
        if (lastCp == CharCodes.SEMI.code) {
            this.consumed += 1
        } else if (this.decodeMode === DecodingMode.Strict) {
            return 0
        }

        this.emitCodePoint(replaceCodePoint(this.result), this.consumed)

        if (this.errors != null) {
            if (lastCp != CharCodes.SEMI.code) {
                this.errors.missingSemicolonAfterCharacterReference()
            }

            this.errors.validateNumericCharacterReference(this.result)
        }

        return this.consumed
    }

    /**
     * Parses a named entity.
     *
     * Equivalent to the `Named character reference state` in the HTML spec.
     *
     * @param str The string containing the entity (or a continuation of the entity).
     * @param offset The current offset.
     * @returns The number of characters that were consumed, or -1 if the entity is incomplete.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun stateNamedEntity(str: String, offset: Int): Int {
        var current = decodeTree[this.treeIndex].toInt()
        // The mask is the number of bytes of the value, including the current byte.
        var valueLength = (current.toInt() and BinTrieFlags.VALUE_LENGTH.flag) shr 14

        var offset = offset

        while (offset < str.length) {
            offset++
            this.excess++

            val char = str[offset].code

            this.treeIndex = determineBranch(
                decodeTree,
                current,
                this.treeIndex + max(1, valueLength),
                char
            )

            if (this.treeIndex < 0) {
                return if (
                    this.result == 0 ||
                    // If we are parsing an attribute
                    (
                        this.decodeMode === DecodingMode.Attribute &&
                        // We shouldn't have consumed any characters after the entity,
                        (
                            valueLength == 0 ||
                            // And there should be no invalid characters.
                            isEntityInAttributeInvalidEnd(char)
                        )
                    )
                ) 0
                else this.emitNotTerminatedNamedEntity()
            }

            current = decodeTree[this.treeIndex].toInt()
            valueLength = (current and BinTrieFlags.VALUE_LENGTH.flag) shr 14

            // If the branch is a value, store it and continue
            if (valueLength != 0) {
                // If the entity is terminated by a semicolon, we are done.
                if (char == CharCodes.SEMI.code) {
                    return this.emitNamedEntityData(
                        this.treeIndex,
                        valueLength,
                        this.consumed + this.excess
                    )
                }

                // If we encounter a non-terminated (legacy) entity while parsing strictly, then ignore it.
                if (this.decodeMode !== DecodingMode.Strict) {
                    this.result = this.treeIndex
                    this.consumed += this.excess
                    this.excess = 0
                }
            }
        }

        return -1
    }

    /**
     * Emit a named entity that was not terminated with a semicolon.
     *
     * @returns The number of characters consumed.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun emitNotTerminatedNamedEntity(): Int {
        val valueLength = (decodeTree[result].toInt() and BinTrieFlags.VALUE_LENGTH.flag) shr 14

        this.emitNamedEntityData(result, valueLength, this.consumed)
        this.errors?.missingSemicolonAfterCharacterReference()

        return this.consumed
    }

    /**
     * Emit a named entity.
     *
     * @param result The index of the entity in the decode tree.
     * @param valueLength The number of bytes in the entity.
     * @param consumed The number of characters consumed.
     *
     * @returns The number of characters consumed.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun emitNamedEntityData(
        result: Int,
        valueLength: Int,
        consumed: Int
    ): Int {
        this.emitCodePoint(
            if (valueLength == 1) decodeTree[result].toInt() and BinTrieFlags.VALUE_LENGTH.flag.inv()
            else decodeTree[result + 1].toInt(),
            consumed
        )
        if (valueLength == 3) {
            // For multi-byte values, we need to emit the second byte.
            this.emitCodePoint(decodeTree[result + 2].toInt(), consumed)
        }

        return consumed
    }

    /**
     * Signal to the parser that the end of the input was reached.
     *
     * Remaining data will be emitted and relevant errors will be produced.
     *
     * @returns The number of characters consumed.
     */
    fun end(): Int {
        when (this.state) {
            EntityDecoderState.NamedEntity -> {
                // Emit a named entity if we have one.
                return if (
                    this.result != 0 &&
                    (
                        this.decodeMode != DecodingMode.Attribute ||
                        this.result == this.treeIndex
                    )
                ) this.emitNotTerminatedNamedEntity()
                else 0
            }
            // Otherwise, emit a numeric entity if we have one.
            EntityDecoderState.NumericDecimal -> {
                return this.emitNumericEntity(0, 2)
            }
            EntityDecoderState.NumericHex -> {
                return this.emitNumericEntity(0, 3)
            }
            EntityDecoderState.NumericStart -> {
                this.errors?.absenceOfDigitsInNumericCharacterReference(
                    this.consumed
                )
                return 0
            }
            EntityDecoderState.EntityStart -> {
                // Return 0 if we have no entity.
                return 0
            }
        }
    }

    companion object {
        const val TO_LOWER_BIT = 0b100000

        fun isNumber(code: Int): Boolean {
            return code >= CharCodes.ZERO.code && code <= CharCodes.NINE.code
        }

        fun isHexadecimalCharacter(code: Int): Boolean {
            return (
                (code >= CharCodes.UPPER_A.code && code <= CharCodes.UPPER_F.code) ||
                (code >= CharCodes.LOWER_A.code && code <= CharCodes.LOWER_F.code)
            )
        }

        fun isAsciiAlphaNumeric(code: Int): Boolean {
            return (
                (code >= CharCodes.UPPER_A.code && code <= CharCodes.UPPER_Z.code) ||
                (code >= CharCodes.LOWER_A.code && code <= CharCodes.LOWER_Z.code) ||
                isNumber(code)
            )
        }

        /**
         * Checks if the given character is a valid end character for an entity in an attribute.
         *
         * Attribute values that aren't terminated properly aren't parsed, and shouldn't lead to a parser error.
         * See the example in https://html.spec.whatwg.org/multipage/parsing.html#named-character-reference-state
         */
        fun isEntityInAttributeInvalidEnd(code: Int): Boolean {
            return code == CharCodes.EQUALS.code || isAsciiAlphaNumeric(code)
        }

        /**
         * Creates a function that decodes entities in a string.
         *
         * @param decodeTree The decode tree.
         * @returns A function that decodes entities in a string.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun getDecoder(decodeTree: UShortArray): (str: String, decodeMode: DecodingMode) -> String {
            var ret = ""
            val decoder = EntityDecoder(
                decodeTree = decodeTree,
                emitCodePoint = { cp, _ ->
                    ret += fromCodePoint(cp)
                }
            )

            return { str, decodeMode ->
                var lastIndex = 0
                var offset = 0

                while (true) {
                    offset = str.indexOf('&', offset)

                    if (offset < 0) {
                        break
                    }

                    ret += str.substring(lastIndex, offset)

                    decoder.startEntity(decodeMode)

                    val len = decoder.write(
                        str = str,
                        // Skip the "&"
                        offset = offset + 1
                    )

                    if (len < 0) {
                        lastIndex = offset + decoder.end();
                        break
                    }

                    lastIndex = offset + len
                    // If `len` is 0, skip the current `&` and continue.
                    offset = if (len == 0) lastIndex + 1 else lastIndex
                }

                val result = ret + str.substring(0, lastIndex)

                // Make sure we don't keep a reference to the final string.
                ret = "";

                result
            }
        }

        /**
         * Determines the branch of the current node that is taken given the current
         * character. This function is used to traverse the trie.
         *
         * @param decodeTree The trie.
         * @param current The current node.
         * @param nodeIdx The index right after the current node and its value.
         * @param char The current character.
         * @returns The index of the next node, or -1 if no branch is taken.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun determineBranch(
            decodeTree: UShortArray,
            current: Int,
            nodeIdx: Int,
            char: Int
        ): Int {
            val branchCount = (current and BinTrieFlags.BRANCH_LENGTH.flag) shr 7
            val jumpOffset = current and BinTrieFlags.JUMP_TABLE.flag

            // Case 1: Single branch encoded in jump offset
            if (branchCount == 0) {
                return if (jumpOffset != 0 && char == jumpOffset) nodeIdx else -1
            }

            // Case 2: Multiple branches encoded in jump table
            if (jumpOffset != 0) {
                val value = char - jumpOffset

                return if (value < 0 || value >= branchCount) -1
                    else decodeTree[nodeIdx + value].toInt() - 1;
            }

            // Case 3: Multiple branches encoded in dictionary

            // Binary search for the character.
            var lo = nodeIdx
            var hi = lo + branchCount - 1

            while (lo <= hi) {
                val mid = (lo + hi) ushr 1
                val midVal = decodeTree[mid].toInt()

                if (midVal < char) {
                    lo = mid + 1;
                } else if (midVal > char) {
                    hi = mid - 1;
                } else {
                    return decodeTree[mid + branchCount].toInt()
                }
            }

            return -1
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        val htmlDecoder = getDecoder(decodeDataHtml)
//        val htmlDecoder = getDecoder(htmlDecodeTree)
        @OptIn(ExperimentalUnsignedTypes::class)
        val xmlDecoder = getDecoder(ushortArrayOf())
//        val xmlDecoder = getDecoder(xmlDecodeTree)

        /**
         * Decodes an HTML string.
         *
         * @param str The string to decode.
         * @param mode The decoding mode.
         * @returns The decoded string.
         */
        fun decodeHTML(str: String, mode: DecodingMode = DecodingMode.Legacy): String {
            return htmlDecoder(str, mode)
        }

        /**
         * Decodes an HTML string in an attribute.
         *
         * @param str The string to decode.
         * @returns The decoded string.
         */
        fun decodeHTMLAttribute(str: String): String {
            return htmlDecoder(str, DecodingMode.Attribute)
        }

        /**
         * Decodes an HTML string, requiring all entities to be terminated by a semicolon.
         *
         * @param str The string to decode.
         * @returns The decoded string.
         */
        fun decodeHTMLStrict(str: String): String {
            return htmlDecoder(str, DecodingMode.Strict);
        }

        /**
         * Decodes an XML string, requiring all entities to be terminated by a semicolon.
         *
         * @param str The string to decode.
         * @returns The decoded string.
         */
        fun decodeXML(str: String): String {
            return xmlDecoder(str, DecodingMode.Strict);
        }
    }

}