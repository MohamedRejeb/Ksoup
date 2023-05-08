package com.mohamedrejeb.ksoup.scripts.trie

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log2

fun binaryLength(num: Int): Float {
    return ceil(log2(num.toFloat()))
}

fun encodeNode(
    node: TrieNode,
    encodeCache: MutableMap<TrieNode, Int>,
    enc: MutableList<Int>,
    maxJumpTableOverhead: Int,
): Int {
    // Cache nodes, as we can have loops
    val cached = encodeCache[node]
    if (cached != null) return cached

    val startIndex = enc.size

    encodeCache[node] = startIndex

    enc.add(0)
    val nodeIdx = enc.lastIndex

    node.value?.let { nodeValue ->
        var valueLength = 0

        /*
         * If we don't have a branch and the value is short, we can
         * store the value in the node.
         */
        if (node.next != null ||
            nodeValue.length > 1 ||
            binaryLength(nodeValue[0].code) > 14
        ) {
            valueLength = nodeValue.length
        }

        // Add 1 to the value length, to signal that we have a value.
        valueLength += 1

        check(binaryLength(valueLength) <= 2) { "Too many bits for value length" }

        enc[nodeIdx] = enc[nodeIdx] or (valueLength shl 14)

        if (valueLength == 1) {
            enc[nodeIdx] = enc[nodeIdx] or nodeValue[0].code
        } else {
            for (element in nodeValue) {
                enc.add(element.code)
            }
        }
    }

    node.next?.let {
        addBranches(
            next = it,
            nodeIdx = nodeIdx,
            encodeCache = encodeCache,
            enc = enc,
            maxJumpTableOverhead = maxJumpTableOverhead,
        )
    }

    println("Node: $nodeIdx")
    println("Start: $startIndex")
    println("Value: ${node.value}")
    check(nodeIdx == startIndex) { "Has expected location" }

    return startIndex
}

fun addBranches(
    next: MutableMap<Int, TrieNode>,
    nodeIdx: Int,
    encodeCache: MutableMap<TrieNode, Int>,
    enc: MutableList<Int>,
    maxJumpTableOverhead: Int,
) {
    val branches = next.toList()

    // Sort branches ASC by key
    branches.sortedBy { it.first }

    println(branches)

    check(binaryLength(branches.size) <= 6) { "Too many bits for branches" }

    // If we only have a single branch, we can write the next value directly
    if (branches.size == 1 && !encodeCache.containsKey(branches[0].second)) {
        val (char, next) = branches[0]

        check(binaryLength(char) <= 7) { "Too many bits for single char" }

        enc[nodeIdx] = enc[nodeIdx] or char
        encodeNode(
            node = next,
            encodeCache = encodeCache,
            enc = enc,
            maxJumpTableOverhead = maxJumpTableOverhead,
        )
        return
    }

    val branchIndex = enc.size

    // If we have consecutive branches, we can write the next value as a jump table

    /*
     * First, we determine how much space adding the jump table adds.
     *
     * If it is more than 2x the number of branches (which is equivalent
     * to the size of the dictionary), skip it.
     */

    val jumpOffset = branches[0].first
    val jumpEndValue = branches[branches.lastIndex].first

    println("jumpOffset: $jumpOffset")
    println("jumpEndValue: $jumpEndValue")

    val jumpTableLength = jumpEndValue - jumpOffset + 1

    val jumpTableOverhead = jumpTableLength / branches.size

    println("jumpTableOverhead: $jumpTableOverhead")

    if (jumpTableOverhead <= maxJumpTableOverhead) {
        check(binaryLength(jumpOffset) <= 16) { "Offset $jumpOffset too large at ${binaryLength(jumpOffset)}" }

        // Write the length of the adjusted table, plus jump offset
        enc[nodeIdx] = enc[nodeIdx] or ((jumpTableLength shl 7) or jumpOffset)

        println("jumpTableLength: $jumpTableLength")
        check(binaryLength(jumpTableLength) <= 7) { "Too many bits (${binaryLength(jumpTableLength)}) for branches" }

        // Reserve space for the jump table
        repeat(jumpTableLength) {
            enc.add(0)
        }

        println("branches.size: ${branches.size}")
        println("enc.size: ${enc.size}")

        // Write the jump table
        for ((char, next) in branches) {
            val index = char - jumpOffset
            // Write all values + 1, so 0 will result in a -1 when decoding
            println("index: $index")
            println("newIndex: ${branchIndex + index}")

            println("enc.size before: ${enc.size}")
            val offset = encodeNode(
                node = next,
                encodeCache = encodeCache,
                enc = enc,
                maxJumpTableOverhead = maxJumpTableOverhead,
            ) + 1
            println("enc.size after: ${enc.size}")
            enc[branchIndex + index] = offset
        }

        return
    }

    enc[nodeIdx] = enc[nodeIdx] or (branches.size shl 7)

    enc.addAll(branches.map { it.first })

    // Reserve space for destinations, using a value that is out of bounds
    repeat(branches.size) {
        enc.add(Int.MAX_VALUE)
    }

    check(enc.size == branchIndex + branches.size * 2) { "Did not reserve enough space" }

    // Encode the branches
    branches.forEachIndexed { idx, (value, next) ->
        check(value < 128) { "Branch value too large" }

        val currentIndex = branchIndex + branches.size + idx
        check(enc[currentIndex - branches.size] == value) { "Should have the value as the first element" }
        check(enc[currentIndex] == Int.MAX_VALUE) { "Should have the placeholder as the second element" }
        val offset = encodeNode(
            node = next,
            encodeCache = encodeCache,
            enc = enc,
            maxJumpTableOverhead = maxJumpTableOverhead,
        )

        check(binaryLength(offset) <= 16) { "Too many bits for offset" }
        enc[currentIndex] = offset
    }
}

fun encodeTrie(trie: TrieNode, maxJumpTableOverhead: Int = 2): IntArray {
    val encodeCache = mutableMapOf<TrieNode, Int>()
    val enc = mutableListOf<Int>()

    encodeNode(
        node = trie,
        encodeCache = encodeCache,
        enc = enc,
        maxJumpTableOverhead = maxJumpTableOverhead
    )

    // Make sure that every value fits in a UInt16
    check(enc.all { it >= 0 && binaryLength(it) <= 16 }) { "Too many bits" }

    return enc.toIntArray()
}