package com.mohamedrejeb.ksoup.utils

import kotlin.math.pow

fun Int.pow(x: Int): Int {
    return this.toDouble().pow(x).toInt()
}