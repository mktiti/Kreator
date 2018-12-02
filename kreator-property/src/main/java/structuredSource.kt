package hu.mktiti.kreator.property

import hu.mktiti.kreator.annotation.Injectable
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@Injectable(tags = ["structured-file"])
class StructuredFileSource(
        filePath: String = System.getenv(PROPS_FILE_ENV_KEY)
) : PropertiesSource {

    private val radixRoot: RadixNode<String, String> =
            namedNodesToRadix("", parseContentList(File(filePath).readText()))

    override fun safeProperty(parts: List<String>): String? = radixRoot[parts]

}

fun splitAtAll(s: String, indices: List<Int>): List<String> {
    if (s.isEmpty()) return listOf(s)

    val postIndices = ArrayList<Int>(indices.size + 1)
    postIndices.addAll(indices)
    postIndices.add(s.length)

    val list = LinkedList<String>()
    var prev = 0
    for (i in postIndices) {
        list.add(s.substring(prev, i))
        prev = i
    }

    return list
}

fun splitNamedBlock(content: String): Pair<String, String?> {
    var cleaned = content.trim()

    if (cleaned.lastOrNull() != '}') {
        if (cleaned.contains('{') || cleaned.isBlank())
            throw PropertyConfigException("Invalid block '$content'")
        else
            return cleaned to null
    }
    cleaned = cleaned.dropLast(1)

    val opening = cleaned.indexOf('{')
    if (opening == -1) throw PropertyConfigException("Invalid block '$content'")

    val name = cleaned.subSequence(startIndex = 0, endIndex = opening).trim().toString()
    if (name.isBlank()) throw PropertyConfigException("Invalid block '$content'")
    if (opening == cleaned.length - 1) return name to null

    return name to cleaned.subSequence(opening + 1, cleaned.length).trim().toString()
}

fun parseProperty(line: String): Pair<String, String>? {
    val split = line.split(":").filter(String::isNotBlank).map(String::trim)
    return when (split.size) {
        0, 1 -> null
        else -> split[0].toLowerCase() to split.drop(1).joinToString(separator = ":")
    }
}

data class NamedNode(val name: String, val node: RadixNode<String, String>)

fun parseBlock(string: String): NamedNode {
    val (fullName, content) = splitNamedBlock(string)

    val rootProperty = parseProperty(fullName)
    val (name, value) = rootProperty ?: (fullName.toLowerCase() to null)

    val node = content?.let {
        namedNodesToRadix(value, parseContentList(it))
    } ?: RadixNode(value)

    return NamedNode(name, node)
}

fun parseContentList(s: String): List<NamedNode> {
    val helper = s.map {
        when (it) {
            '{' -> 1
            '}' -> -1
            else -> 0
        }
    }

    val array = IntArray(s.length) {0}
    var currLevel = 0
    s.indices.forEach {
        currLevel += helper[it]
        array[it] = currLevel
        currLevel += helper[it]
    }

    val indices = s.mapIndexed { i, c ->
        if (c == ',' || c == '\n') i else null
    }.filter {
        it != null && array[it] == 0
    }.requireNoNulls()

    return splitAtAll(s, indices)
            .map { it.removePrefix(",") }
            .filter(String::isNotBlank)
            .map { parseBlock(it) }
}

fun namedNodesToRadix(rootValue: String?, nodes: List<NamedNode>): RadixNode<String, String> =
        RadixNode(rootValue, nodes.map { it.name to it.node }.toMap())