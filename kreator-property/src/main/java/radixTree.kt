package hu.mktiti.kreator.property

data class RadixNode<K, V>(
        val value: V?,
        val map: Map<K, RadixNode<K, V>> = emptyMap()
) {

    operator fun get(keyParts: List<K>): V? = if (keyParts.isEmpty()) {
        value
    } else {
        map[keyParts.first()]?.get(keyParts.drop(1))
    }

    fun add(keyParts: List<K>, newValue: V): RadixNode<K, V> {
        return if (keyParts.isEmpty()) {
            RadixNode(newValue, map)
        } else {
            val key = keyParts.first()
            val keyTail = keyParts.drop(1)

            val newMap = map.toMutableMap()
            newMap[key] = map[key]?.add(keyTail, newValue) ?: newNodeChain(keyTail, newValue)

            RadixNode(value, newMap)
        }
    }
}

fun <K, V> newNodeChain(keyParts: List<K>, value: V): RadixNode<K, V> = if (keyParts.isEmpty()) {
    RadixNode(value)
} else {
    RadixNode(null, mapOf(keyParts.first() to newNodeChain(keyParts.drop(1), value)))
}

fun createRadixTree(lines: Iterable<Pair<String, String>>, separators: List<String>): RadixNode<String, String> {
    var root = RadixNode<String, String>(null)
    for (line in lines) {
        val split = line.first.split(delimiters = *separators.toTypedArray(), ignoreCase = true)
                        .filter(String::isNotBlank)
                        .map(String::toLowerCase)
        root = root.add(split, line.second)
    }
    return root
}