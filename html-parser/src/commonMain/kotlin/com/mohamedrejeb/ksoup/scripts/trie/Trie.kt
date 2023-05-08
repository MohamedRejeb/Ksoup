package com.mohamedrejeb.ksoup.scripts.trie

import com.mohamedrejeb.ksoup.utils.allIndexed

class TrieNode(
    var value: String? = null,
    var next: MutableMap<Int, TrieNode>? = null
)

fun getTrie(
    map: Map<String, String>,
    legacy: Map<String, String>
): TrieNode {
    val trie = mutableMapOf<Int, TrieNode>()
    val root = TrieNode(next = trie)

    map.keys.forEach { key ->
        // Resolve the key
        var lastMap = trie
        var next: TrieNode? = null

        key.forEach { char ->
            val charCode = char.code
            next = lastMap[charCode] ?: TrieNode()
            lastMap[charCode] = next ?: TrieNode()
            lastMap = next?.next ?: run {
                next?.next = mutableMapOf()
                next?.next!!
            }
        }

        if (key in legacy) next?.value = map[key]

        lastMap[';'.code] = TrieNode(value = map[key])
    }

    fun isEqual(node1: TrieNode, node2: TrieNode): Boolean {
        if (node1 === node2) return true

        if (node1.value !== node2.value) {
            return false
        }

        // Check if the next nodes are equal. That means both are undefined.
        if (node1.next === node2.next) return true
        if (
            node1.next == null ||
            node2.next == null ||
            node1.next?.size != node2.next?.size
        ) {
            return false
        }

        val next1 = node1.next?.toList() ?: emptyList()
        val next2 = node2.next?.toList() ?: emptyList()

        return next1.allIndexed { index, (char1, node1) ->
            val (char2, node2) = next2[index]
            char1 == char2 && isEqual(node1, node2)
        }
    }

    fun mergeDuplicates(node: TrieNode) {
        val nodes = mutableListOf(node)

        var nodeIdx = 0
        while (nodeIdx < nodes.size) {
            val next = nodes[nodeIdx].next
            if (next == null) {
                nodeIdx++
                continue
            }

            for ((char, node) in next.toList()) {
                val idx = nodes.indexOfFirst { isEqual(it, node) }

                if (idx >= 0) {
                    next[char] = nodes[idx]
                } else {
                    nodes.add(node)
                }
            }

            nodeIdx++
        }
    }


    println("enter mergeDuplicates")
    mergeDuplicates(root)
    println("end mergeDuplicates")

    return root
}