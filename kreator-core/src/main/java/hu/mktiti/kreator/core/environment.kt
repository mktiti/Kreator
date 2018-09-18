package hu.mktiti.kreator.core

enum class EnvironmentCompare(val parentOf: Boolean, val childrenOf: Boolean) {
    NONE(false, false),
    EXACT_MATCH(true, true),
    REAL_PARENT_OF(parentOf = true, childrenOf = false),
    REAL_CHILDREN_OF(parentOf = false, childrenOf = true)
}

private val envCompareMatchOrder = listOf(EnvironmentCompare.EXACT_MATCH, EnvironmentCompare.REAL_CHILDREN_OF, EnvironmentCompare.REAL_PARENT_OF)

class InjectEnvironment(
        private val parts: List<String> = listOf()
) {

    companion object {
        fun safeParse(name: String): InjectEnvironment {
            return InjectEnvironment(name.split(".").map { it.trim() }.filterNot { it.isEmpty() })
        }
    }

    fun parentOf(other: InjectEnvironment) = relationshipWith(other).parentOf

    fun childrenOf(other: InjectEnvironment) = relationshipWith(other).childrenOf

    fun relationshipWith(other: InjectEnvironment): EnvironmentCompare = when {
        (parts == other.parts) -> EnvironmentCompare.EXACT_MATCH
        (parts.size < other.parts.size && parts == other.parts.subList(0, parts.size)) -> EnvironmentCompare.REAL_PARENT_OF
        (parts.size > other.parts.size && parts.subList(0, other.parts.size) == other.parts) -> EnvironmentCompare.REAL_CHILDREN_OF
        else -> EnvironmentCompare.NONE
    }

    override fun toString() = parts.joinToString(prefix = "@[", separator = ".", postfix = "]")

    override fun equals(other: Any?) = (other is InjectEnvironment) && (relationshipWith(other) == EnvironmentCompare.EXACT_MATCH)

    override fun hashCode() = parts.hashCode()

}

fun <T> filterEnvironmentsSorted(
        environmentMapping: Collection<Pair<InjectEnvironment, T>>,
        environment: InjectEnvironment
): List<T> {

    val compareCategories = environmentMapping.map { prod ->
        prod.first.relationshipWith(environment) to prod
    }.filterNot {
        it.first == EnvironmentCompare.NONE
    }.groupBy { it.first }

    for (compare in envCompareMatchOrder) {
        val producers = compareCategories[compare] ?: continue
        if (producers.isNotEmpty()) {
            return producers.map { it.second.second }
        }
    }

    return listOf()
}