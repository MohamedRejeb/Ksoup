package com.mohamedrejeb.ksoup.tokenizer

@OptIn(ExperimentalUnsignedTypes::class)
object Sequences {

    val Cdata = ubyteArrayOf(67u, 68u, 65u, 84u, 65u, 91u)

    val CdataEnd = ubyteArrayOf(93u, 93u, 62u)

    val CommentEnd = ubyteArrayOf(45u, 45u, 62u)

    val ScriptEnd = ubyteArrayOf(60u, 47u, 115u, 99u, 114u, 105u, 112u, 116u)

    val StyleEnd = ubyteArrayOf(60u, 47u, 115u, 116u, 121u, 108u, 101u)

    val titleEnd = ubyteArrayOf(60u, 47u, 116u, 105u, 116u, 108u, 101u)

}