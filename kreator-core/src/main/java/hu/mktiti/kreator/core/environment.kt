package hu.mktiti.kreator.core

class InjectEnvironment(
        private val parts: List<String> = listOf()
) {

    constructor(vararg parts: String) : this(parts.filterNot(String::isBlank))

    companion object {
        fun safeParse(name: String): InjectEnvironment {
            return InjectEnvironment(name.split(".").map { it.trim() }.filterNot { it.isEmpty() })
        }
    }

    fun nodeDistance(other: InjectEnvironment): Int? = when {
        (parts == other.parts) -> 0
        (parts.size < other.parts.size && parts == other.parts.subList(0, parts.size)) -> -1
        (parts.size > other.parts.size && parts.subList(0, other.parts.size) == other.parts) -> parts.size - other.parts.size
        else -> null
    }

    override fun toString() = parts.joinToString(prefix = "@[", separator = ".", postfix = "]")

    override fun equals(other: Any?) = (other is InjectEnvironment) && (nodeDistance(other) == 0)

    override fun hashCode() = parts.hashCode()

}

fun <T> filterEnvironmentsSorted(
        environmentMapping: Collection<Pair<InjectEnvironment, T>>,
        environment: InjectEnvironment
): List<T> {

    val compareCategories: Map<Int?, List<T>> = environmentMapping.map { prod ->
        environment.nodeDistance(prod.first) to prod.second
    }.groupBy(keySelector = {it.first}, valueTransform = {it.second})

    compareCategories[0]?.let { return it }

    return compareCategories.filterKeys { it != null }
            .entries.sortedBy { it.key }
            .firstOrNull()?.value ?: emptyList()
}