package hu.mktiti.kreator.property

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import java.io.FileInputStream
import java.util.*

private val PROPS_PART_SEPARATORS = listOf(".", "_", "-", ":")

private const val PROPS_PREFIX_ENV_KEY = "KREATOR_PROPS_PREFIX"
private const val PROPS_FILE_ENV_KEY = "KREATOR_PROPS_FILE"
private const val PROPS_DEFAULT_PREFIX = "kreator-props."
private const val PROPS_DEFAULT_PREFIX_ENV = "KREATOR_PROPS_"

@InjectableType
interface PropertiesSource {

    fun safeProperty(parts: List<String>): String?

    fun safeProperty(key: String): String? = safeProperty(
            key.split(*PROPS_PART_SEPARATORS.toTypedArray()).map(String::toLowerCase)
    )

}

object EmptySource : PropertiesSource {
    override fun safeProperty(parts: List<String>): String? = null

    override fun safeProperty(key: String): String? = null
}

abstract class MapPropertiesSource(
        private val prefix: String = System.getenv(PROPS_PREFIX_ENV_KEY) ?: PROPS_DEFAULT_PREFIX
) : PropertiesSource {

    private val radixRoot: RadixNode<String, String> = loadToTree()

    private fun loadToTree(): RadixNode<String, String> {
        val lines = loadPropData().filter { (k, _) ->
            k.startsWith(prefix) && k.length > prefix.length
        }.map { (k, v) ->
            k.substring(prefix.length) to v
        }

        return createRadixTree(lines, PROPS_PART_SEPARATORS)
    }

    abstract fun loadPropData(): Map<String, String>

    override fun safeProperty(parts: List<String>) = radixRoot[parts]
}

@Injectable(tags = ["env-var"], default = true)
class EnvVarPropertiesSource(
        prefix: String = System.getenv(PROPS_PREFIX_ENV_KEY) ?: PROPS_DEFAULT_PREFIX_ENV
) : MapPropertiesSource(prefix) {

    override fun loadPropData(): Map<String, String> = System.getenv()

}

private fun Properties.toSafeMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    forEach { k, v ->
        if (k is String && v is String) {
            map[k] = v
        }
    }
    return map
}

@Injectable(tags = ["sys-props"])
class SystemPropertiesSource(
        prefix: String = System.getenv(PROPS_PREFIX_ENV_KEY) ?: PROPS_DEFAULT_PREFIX
) : MapPropertiesSource(prefix) {

    override fun loadPropData(): Map<String, String> = System.getProperties().toSafeMap()

}

@Injectable(tags = ["file-props"])
class PropertiesFileSource(
        prefix: String = System.getenv(PROPS_PREFIX_ENV_KEY) ?: PROPS_DEFAULT_PREFIX,
        private val filePath: String = System.getenv(PROPS_FILE_ENV_KEY)
) : MapPropertiesSource(prefix) {

    override fun loadPropData(): Map<String, String> =
        FileInputStream(filePath).use { fis ->
            with(Properties()) {
                load(fis)
                toSafeMap()
            }
        }

}