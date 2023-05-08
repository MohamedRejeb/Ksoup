package com.mohamedrejeb.ksoup.utils

public inline fun <T> Iterable<T>.allIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true
    var index = 0
    for (element in this) if (!predicate(index++, element)) return false
    return true
}