package hu.mktiti.kreator.property

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.*

private val PROPS_PART_SEPARATORS = listOf(".", "_")

private const val PROPS_PREFIX_ENV_KEY = "KREATOR_PROPS_PREFIX"
internal const val PROPS_FILE_ENV_KEY = "KREATOR_PROPS_FILE"
private const val PROPS_DEFAULT_PREFIX = "kreator-props."
private const val PROPS_DEFAULT_PREFIX_ENV = ""

class PropertyConfigException(message: String) : RuntimeException(message)

private fun splitKey(key: String) =
        key.split(*PROPS_PART_SEPARATORS.toTypedArray()).map(String::toLowerCase)

@InjectableType
interface PropertiesSource {

    fun safeProperty(parts: List<String>): String?

    fun safeProperty(key: String): String? = safeProperty(splitKey(key))

    fun safeIntProperty(parts: List<String>): Int? = safeProperty(parts)?.toIntOrNull()

    fun safeIntProperty(key: String): Int? = safeIntProperty(splitKey(key))

    fun safeBoolProperty(parts: List<String>): Boolean? = safeProperty(parts)?.toBoolean()

    fun safeBoolProperty(key: String): Boolean? = safeBoolProperty(splitKey(key))

    fun safeDateProperty(parts: List<String>): LocalDateTime? = safeProperty(parts)?.let {
        try {
            LocalDateTime.parse(it)
        } catch (dfe: DateTimeParseException) {
            null
        }
    }

    fun safeDateProperty(key: String): LocalDateTime? = safeDateProperty(splitKey(key))

}

object EmptySource : PropertiesSource {
    override fun safeProperty(parts: List<String>): String? = null

    override fun safeProperty(key: String): String? = null
}

abstract class RadixPropertiesSource(
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
) : RadixPropertiesSource(prefix) {

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
) : RadixPropertiesSource(prefix) {

    override fun loadPropData(): Map<String, String> = System.getProperties().toSafeMap()

}

@Injectable(tags = ["file-props"])
class PropertiesFileSource(
        prefix: String = System.getenv(PROPS_PREFIX_ENV_KEY) ?: PROPS_DEFAULT_PREFIX,
        private val filePath: String = System.getenv(PROPS_FILE_ENV_KEY)
) : RadixPropertiesSource(prefix) {

    override fun loadPropData(): Map<String, String> =
        FileInputStream(filePath).use { fis ->
            with(Properties()) {
                load(fis)
                toSafeMap()
            }
        }

}