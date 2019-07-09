package hu.mktiti.kreator.property

import com.moandjiezana.toml.Toml
import hu.mktiti.kreator.annotation.Injectable
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

@Injectable(tags = ["toml"])
class TomlSource(
        filePath: String = System.getenv(PROPS_FILE_ENV_KEY)
) : PropertiesSource {

    private val data = Toml()

    init {
        data.read(File(filePath))
    }

    private fun <T> onParentSafe(parts: List<String>, code: Toml.(String) -> T?): T? {
        if (parts.isEmpty()) {
            return null
        }

        var current: Toml = data
        for (i in 0 until (parts.size - 1)) {
            current = current.getTable(parts[i])
            if (current == null) {
                return null
            }
        }
        return current.code(parts.last())
    }

    override fun safeProperty(parts: List<String>): String? =
        onParentSafe(parts) {
            getString(it)
        }

    override fun safeBoolProperty(parts: List<String>): Boolean? =
        onParentSafe(parts) {
            getBoolean(it)
        }

    override fun safeIntProperty(parts: List<String>): Int? =
        onParentSafe(parts) {
            getLong(it)?.toInt()
        }


    override fun safeDateProperty(parts: List<String>): LocalDateTime? =
        onParentSafe(parts) {
            getDate(it)?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        }


}